package com.iscas.strategy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iscas.bean.*;
import com.iscas.bean.Assertion;
import com.iscas.bean.fault.Abort;
import com.iscas.bean.fault.Delay;
import com.iscas.bean.fault.Fault;
import com.iscas.bean.result.*;
import com.iscas.service.Injector;
import com.iscas.service.Jaeger;
import com.iscas.service.Telemetry;
import com.iscas.service.TraceTracker;
import com.iscas.util.Cmd;
import com.iscas.util.FileUtil;
import com.iscas.util.Time;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Heuristic {

    public static int times = 0;

    @Autowired
    public Heuristic(Configuration configuration, SimpMessagingTemplate simpMessagingTemplate, RestTemplate restTemplate) {
        this.configuration = configuration;
        this.logPusher = simpMessagingTemplate;
        this.restTemplate = restTemplate;
    }

    // configuration
    private Configuration configuration;

    // context
    private String id;
    private String startTime;
    private List<Trace> tracePool;
    private List<Trace> currentTracePool;
    private Set<String> currentUrls;
    private List<String[]> IPS;
    private String[] currentIP;
    private List<Fault> currentFaults;
    private int currentIPIndex;
    private int injectTimes;
    private List<DetectResult> rs;
    private List<TimeoutResult> tors;
    private List<CircuitBreakResult> cbrs;
    private List<RetryResult> rrs;
    private List<BulkHeadResult> bhrs;
    private Set<String> executedIPS;
    private Set<String> failedIPS;

    // utils
    private Logger logger;
    private SimpMessagingTemplate logPusher;
    private RestTemplate restTemplate;


    public void run() {
        init();

        try {
            //抽样
            sampleTraces();

            // 探测
            for (Trace t : this.tracePool) {
                this.currentTracePool = new ArrayList<>();
                this.currentTracePool.add(t);

                detect();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clean();
    }

    public void detect() throws InterruptedException {
        this.executedIPS = new HashSet<>();
        this.failedIPS = new HashSet<>();

        while (true) {
            // 求解IPS
            this.IPS = new SAT().sat(this.currentTracePool);
            filterIPS();
            if (this.IPS.isEmpty())
                break;

            // 逐个探测
            for (String[] IP : this.IPS) {
                this.currentIP = IP;
                this.injectTimes++;
                Injector injector = new Injector();

                // 注入故障
                injectAbort(500);

                // 测试响应
                Assertion failedAssertion = test();

                // 更新
                if (failedAssertion == null) {
                    updateTraces();
                    deleteFaults();
                }
                // 分析
                else {
                    deleteFaults();
                    analysis();
                }
            }
        }
    }

    private void init() {

        this.id = "detect_" + (++Heuristic.times);

        // create logger
        LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender fileAppender = new FileAppender();
        fileAppender.setImmediateFlush(true);
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(this.id + ".log");
        fileAppender.setName(this.id + ".log");
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p 18737 --- [%t] %-40.40logger{39} : %m%n%wEx");
        encoder.start();
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        this.logger = loggerContext.getLogger(this.id);
        this.logger.addAppender(fileAppender);

        this.startTime = Time.getCurTimeStr();
        this.injectTimes = 0;
        this.currentFaults = new ArrayList<>();
    }

    private void sampleTraces() throws InterruptedException {
        int sampleBatch = this.configuration.getTraceDetectSampleBatch();
        long duration = this.configuration.getTraceDetectDuration();
        int interval = this.configuration.getTraceDetectSampleInterval();

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        Map<Integer, Trace> traces = new HashMap<>();

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // sample traces
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {
            // sleep for interval
            Thread.sleep(interval * 1000);

            // sample
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);

            // convert and save
            for (Span root : spanTrees) {
                Trace t = spanToTrace(root);
                int hashCode = t.hashCode();
                if (traces.containsKey(hashCode)) {
                    Trace sameTrace = traces.get(hashCode);
                    sameTrace.increaseCount();

                    Set<String> urls = Arrays.stream(sameTrace.getUrls()).collect(Collectors.toSet());
                    String url = t.getUrls()[0];
                    urls.add(url);
                    sameTrace.setUrls(urls.toArray(new String[0]));
                } else
                    traces.put(hashCode, t);
            }
        }

        this.tracePool = new ArrayList<>(traces.values());

    }

    private void updateTraces() throws InterruptedException {
        int sampleBatch = this.configuration.getTraceDetectSampleBatch();
        long duration = 30; // 抽样30秒
        int interval = this.configuration.getTraceDetectSampleInterval();

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        // 当前测试的接口集合
        Set<String> urls = new HashSet<>();
        for (Trace t : this.currentTracePool)
            urls.addAll(Arrays.asList(t.getUrls()));

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // sample traces
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {
            Thread.sleep(interval * 1000);

            // sample
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);
            for (Span root : spanTrees) {
                Trace t = spanToTrace(root);
                String url = t.getUrls()[0];
                if (urls.contains(url)) {
                    List<String> services = Arrays.asList(t.getServices());
                    for (String exService : this.currentIP)
                        services.remove(exService);

                    if (services.size() > 0) {
                        t.setServices(services.toArray(new String[0]));
                        this.currentTracePool.add(t);
                    }
                }
            }
        }
    }

    private void analysis() throws InterruptedException {
        this.currentUrls = new HashSet<>();
        for (Trace t : this.currentTracePool)
            this.currentUrls.addAll(Arrays.asList(t.getUrls()));
        analysisTimeout();
        analysisRery();
        analysisBulkHead();
        analysisCircuitBreaker();
    }

    private void analysisTimeout() throws InterruptedException {
        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 注入delay故障
        this.injectDelay(20);

        // 参数
        long duration = this.configuration.getTimeoutDetectionDuration();
        int interval = this.configuration.getTimeoutSampleInterval();
        int sampleBatch = this.configuration.getTimeoutSampleBatch();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        // 探测
        Map<Pair<String, String>, Double> responseTimeTotal = new HashMap<>();
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            Map<Pair<String, String>, Long> spanTimeSum = new HashMap<>();
            Map<Pair<String, String>, Integer> spanCounter = new HashMap<>();
            List<Span> roots = tracker.sampleErr(sampleBatch, Math.min(interval, 5));

            // 抽样
            for (Span root : roots) {
                // 如果不是目标接口产生的，则跳过
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;

                // DFS
                LinkedList<Span> stack = new LinkedList<>();
                stack.push(root);
                Pair<String, String> servicePair = new Pair<>("root", root.getService());
                spanTimeSum.put(servicePair, root.getDuration());
                spanCounter.put(servicePair, 1);

                while (!stack.isEmpty()) {
                    Span fatherSpan = stack.pop();
                    for (Span childSpan : fatherSpan.getChildren()) {
                        servicePair = new Pair<>(fatherSpan.getService(), childSpan.getService());

                        // 统计数据
                        if (!spanTimeSum.containsKey(servicePair))
                            spanTimeSum.put(servicePair, 0L);
                        spanTimeSum.put(servicePair, spanTimeSum.get(servicePair) + childSpan.getDuration());
                        if (!spanCounter.containsKey(servicePair))
                            spanCounter.put(servicePair, 0);
                        spanCounter.put(servicePair, spanCounter.get(servicePair) + 1);
                    }
                }
            }

            // 求平均值
            for (Pair<String, String> servicePair : spanTimeSum.keySet()) {
                double responseTime = ((double) spanTimeSum.get(servicePair)) / spanCounter.get(servicePair);
                if (responseTimeTotal.containsKey(servicePair))
                    responseTimeTotal.put(servicePair,
                            (responseTime + responseTimeTotal.get(servicePair)) / 2);
                else
                    responseTimeTotal.put(servicePair, responseTime);
            }

            // 暂停一段时间
            Thread.sleep(interval * 1000);

        }

        // 诊断
        Map<String, Double> responseTimeByFather = new HashMap<>();
        for (Pair<String, String> servicePair : responseTimeTotal.keySet()) {
            String father = servicePair.getKey();
            if (!responseTimeByFather.containsKey(father))
                responseTimeByFather.put(father, 0.0);
            responseTimeByFather.put(father, responseTimeByFather.get(father) + responseTimeTotal.get(servicePair));
        }

        // 生成结果
        for (Pair<String, String> servicePair : responseTimeTotal.keySet()) {
            String message = "无异常";
            if (responseTimeTotal.get(servicePair) <= responseTimeByFather.get(servicePair.getKey()))
                message = servicePair.getKey() + "阈值过小";
            TimeoutResult rs = new TimeoutResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue()
                    , responseTimeTotal.get(servicePair), message, Time.getCurTimeStr());
            this.tors.add(rs);
        }

        // 删除故障
        this.deleteFaults();
    }

    private void analysisCircuitBreaker() throws InterruptedException {
        TraceTracker tracker = new Jaeger(this.restTemplate);
        Span normalSpanTree = null;
        // 对正常情况下调用链采样
        while (normalSpanTree == null) {
            Thread.sleep(10 * 1000);
            List<Span> roots = tracker.sample(10, 10);
            for (Span root : roots)
                if (this.currentUrls.contains(root.getUrl())) {
                    normalSpanTree = root;
                    break;
                }
        }
        Map<String, Set<String>> edges = new HashMap<>();
        // 获得所有的边
        LinkedList<Span> stack = new LinkedList<>();
        stack.push(normalSpanTree);
        while (!stack.isEmpty()) {
            Span span = stack.pop();
            for (Span child : span.getChildren()) {
                if (!edges.containsKey(span.getService()))
                    edges.put(span.getService(), new HashSet<>());
                edges.get(span.getService()).add(child.getService());
                stack.push(child);
            }
        }


        // 注入故障
        this.injectAbort(500);

        // 参数
        long duration = this.configuration.getCircuitbreakerLocateDuration();
        int interval = this.configuration.getCircuitbreakerLocateInterval();
        int sampleBatch = this.configuration.getCircuitbreakerLocateSampleBatch();
        int windowSize = this.configuration.getCircuitbreakerQPSWindowSize();
        long duration2 = this.configuration.getCircuitbreakerHalfStateDuration();
        int interval2 = this.configuration.getCircuitbreakerInterval();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        Set<Pair<String, String>> locations = new HashSet<>();

        // 探测熔断位置
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {

            List<Span> roots = tracker.sampleErr(sampleBatch, Math.min(5, interval));

            for (Span root : roots) {
                // 如果不是目标接口产生的，则跳过
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;

                // 获取所有边
                stack = new LinkedList<>();
                stack.push(root);
                while (!stack.isEmpty()) {
                    Span span = stack.pop();

                    // 本身就是一个新的分支
                    if (!edges.containsKey(span.getService()))
                        continue;

                    Set<String> childrenNormal = edges.get(span.getService());
                    Set<String> childrenNow = new HashSet<>();
                    for (Span child : span.getChildren()) {
                        String service = child.getService();
                        childrenNow.add(service);

                        // both have
                        if (childrenNormal.contains(service))
                            stack.push(child);
                    }

                    // 求差得到熔断位置
                    Set<String> tmp = new HashSet<>(childrenNormal);
                    tmp.removeAll(childrenNow);
                    for (String childService : tmp)
                        locations.add(new Pair<>(span.getService(), childService));
                }
            }

            //暂停一段时间
            Thread.sleep(interval * 1000);
        }

        // 缺失熔断
        if (locations.isEmpty())
            this.cbrs.add(new CircuitBreakResult(this.id, this.injectTimes,
                    null, null, 0.0, "缺失熔断",
                    Time.getCurTimeStr(), this.currentUrls.toArray(new String[0])));
        else {// 监控半开状态
            startTime = Time.getCurTimeStampMs();
            endTime = startTime + duration2 * 1000;

            Telemetry monitor = new Telemetry(this.restTemplate);
            Map<Pair<String, String>, Double> result = new HashMap<>();
            //探测半开状态
            for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
                String query = "histogram_quantile(0.80, sum(irate(" +
                        "istio_request_duration_seconds_bucket{reporter=\"source\"}[" + windowSize + "s])) " +
                        "by (le,source_app,destination_service_name))";
                DataSeries[] dataSeries = monitor.query(query);

                for (DataSeries ds : dataSeries) {
                    // 过滤不需要的数据列表
                    Map<String, String> tags = ds.getTags();
                    Pair<String, String> servicePair = new Pair<>(tags.get("source_app"), tags.get("destination_service_name"));
                    if (!locations.contains(servicePair))
                        continue;

                    // 记录数据
                    for (Pair<Double, String> value : ds.getData()) {
                        double tmp = Double.valueOf(value.getValue());
                        if (!result.containsKey(servicePair))
                            result.put(servicePair, tmp);
                        else
                            result.put(servicePair, Math.max(result.get(servicePair), tmp));
                    }
                }

                // 暂停一段时间
                Thread.sleep(interval2);
            }

            // 生成探测结果
            for (Pair<String, String> servicePair : result.keySet())
                this.cbrs.add(new CircuitBreakResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue(),
                        result.get(servicePair), "无异常", Time.getCurTimeStr(), this.currentUrls.toArray(new String[0])));
        }

        //删除故障
        this.deleteFaults();
    }

    private void analysisRery() throws InterruptedException {
        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 注入delay故障
        this.injectAbort(500);

        // 参数
        long duration = this.configuration.getRetryDetectionDuration();
        int interval = this.configuration.getRetrySampleInterval();
        int sampleBatch = this.configuration.getRetrySampleBatch();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        Set<String> exclude = new HashSet<>();
        List<RetryResult> rs = new ArrayList<>();

        // 探测
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            List<Span> roots = tracker.sampleErr(sampleBatch, Math.min(5, interval));

            // 抽样
            for (Span root : roots) {
                // 如果不是目标接口产生的，则跳过
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;

                // 深度优先遍历
                if (root.isErr()) {
                    String token = root.getUrl() + root.getService();
                    if (!exclude.contains(token)) {
                        rs.add(new RetryResult(this.id, this.injectTimes, root.getUrl(),
                                "root", root.getService(), 1, "无异常", Time.getCurTimeStr()));
                        exclude.add(token);
                    }
                } else {
                    LinkedList<Span> stack = new LinkedList<>();
                    stack.push(root);
                    while (!stack.isEmpty()) {
                        Span fatherSpan = stack.pop();
                        Map<Pair<String, String>, Integer> counter = new HashMap<>();

                        // 计数
                        for (Span childSpan : fatherSpan.getChildren()) {
                            stack.push(childSpan);
                            if (!childSpan.isErr())
                                continue;
                            Pair<String, String> servicePair = new Pair<>(fatherSpan.getService(), childSpan.getService());
                            if (!counter.containsKey(servicePair))
                                counter.put(servicePair, 0);
                            counter.put(servicePair, counter.get(servicePair) + 1);
                        }

                        // 记录结果
                        for (Pair<String, String> servicePair : counter.keySet()) {
                            String token = root.getUrl() + servicePair.getKey() + servicePair.getValue() + counter.get(servicePair);
                            if (!exclude.contains(token)) {
                                exclude.add(token);
                                rs.add(new RetryResult(this.id, this.injectTimes, root.getUrl(),
                                        servicePair.getKey(), servicePair.getValue(), counter.get(servicePair), "无异常",
                                        Time.getCurTimeStr()));
                            }
                        }
                    }
                }
            }

            // 暂停一段时间
            Thread.sleep(interval * 1000);
        }
        this.rrs.addAll(rs);

        // 删除故障
        this.deleteFaults();
    }

    private void analysisBulkHead() throws InterruptedException {
        // 确定注入点的父节点与兄弟节点所构成的边集合
        Thread.sleep(5 * 1000);
        Graph graph = new Telemetry(this.restTemplate).queryGraph(5);
        List<Pair<String, String>> edges = graph.getEdges();
        // (1) 确定父节点集合
        Set<String> fatherServices = new HashSet<>();
        for (Pair<String, String> edge : edges) {
            int i = 0;
            while (i < this.currentIP.length) {
                if (this.currentIP[i].equals(edge.getValue()))
                    break;
                ++i;
            }
            if (i < this.currentIP.length)
                fatherServices.add(this.currentIP[i]);
        }
        // (2) 确定边集合
        Set<Pair<String, String>> links = new HashSet<>();
        for (Pair<String, String> edge : edges) {
            if (!fatherServices.contains(edge.getKey()))
                continue;
            int i = 0;
            while (i < this.currentIP.length) {
                if (this.currentIP[i].equals(edge.getValue()))
                    break;
                ++i;
            }
            if (i == this.currentIP.length)
                links.add(edge);
        }

        if (links.isEmpty())
            return;

        // 参数
        int windowSize1 = this.configuration.getBulkheadWindowSize1();
        long duration1 = this.configuration.getBulkheadDuration1();
        int interval1 = this.configuration.getBulkheadInterval1();
        int windowSize2 = this.configuration.getBulkheadWindowSize2();
        long duration2 = this.configuration.getBulkheadDuration2();
        int interval2 = this.configuration.getBulkheadInterval2();
        double threshold = this.configuration.getBulkheadThreshold();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration1 * 1000;

        Telemetry monitor = new Telemetry(this.restTemplate);
        Map<Pair<String, String>, Double> normal = new HashMap<>();
        // 获取正常数据
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            String query = "sum(irate(istio_requests_total{reporter=\"source\"}[" + windowSize1
                    + "])) by (source_app,destination_service_name)";
            DataSeries[] dataSeries = monitor.query(query);

            for (DataSeries ds : dataSeries) {
                // 过滤不需要数据列
                Map<String, String> tags = ds.getTags();
                Pair<String, String> servicePair = new Pair<>(tags.get("source_app"), tags.get("destination_service_name"));
                if (!links.contains(servicePair))
                    continue;

                // 取出数据
                for (Pair<Double, String> value : ds.getData()) {
                    Double tmp = Double.valueOf(value.getValue());
                    if (!normal.containsKey(servicePair))
                        normal.put(servicePair, tmp);
                    else
                        normal.put(servicePair, Math.min(normal.get(servicePair), tmp));
                }
            }

            Thread.sleep(interval1 * 1000);
        }

        // 注入故障
        this.injectDelay(20);

        // 检测
        Map<Pair<String, String>, Double> result = new HashMap<>();
        startTime = Time.getCurTimeStampMs();
        endTime = startTime + duration2 * 1000;
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            String query = "sum(irate(istio_requests_total{reporter=\"source\"}[" + windowSize2
                    + "])) by (source_app,destination_service_name)";
            DataSeries[] dataSeries = monitor.query(query);

            for (DataSeries ds : dataSeries) {
                // 过滤不需要数据列
                Map<String, String> tags = ds.getTags();
                Pair<String, String> servicePair = new Pair<>(tags.get("source_app"), tags.get("destination_service_name"));
                if (!normal.containsKey(servicePair))
                    continue;

                // 取出数据
                for (Pair<Double, String> value : ds.getData()) {
                    Double tmp = Double.valueOf(value.getValue());
                    Double tmpOri = normal.get(servicePair);
                    if ((tmpOri - tmp) / tmpOri >= threshold) {
                        if (!result.containsKey(servicePair))
                            result.put(servicePair, tmp);
                        else
                            result.put(servicePair, Math.min(tmp, result.get(servicePair)));
                    }
                }
            }
            //暂停一段时间
            Thread.sleep(interval2 * 1000);
        }

        // 记录结果
        for (Pair<String, String> servicePair : result.keySet())
            this.bhrs.add(new BulkHeadResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue(),
                    normal.get(servicePair), result.get(servicePair), "缺失熔断", Time.getCurTimeStr()));

        //删除故障
        this.deleteFaults();
    }

    private void clean() {
        this.logger.getAppender(this.id + ".log").stop();
        this.logger = null;

        // TODO: save result
    }

    private void pushLog(String log) {
        String topic = "/detection/operation";
        logPusher.convertAndSend(topic, log);
    }

    private Trace spanToTrace(Span root) {
        LinkedList<Span> stack = new LinkedList<>();
        String url = root.getUrl();
        Set<String> services = new HashSet<>();

        // add root
        services.add(root.getUrl());
        stack.push(root);

        // DFS
        while (!stack.isEmpty()) {
            Span fSpan = stack.pop();
            Span[] children = fSpan.getChildren();
            for (int idx = children.length - 1; idx >= 0; --idx) {
                Span child = children[idx];
                services.add(child.getService());
                stack.push(child);
            }
        }

        return new Trace(new String[]{url}, services.toArray(new String[0]));
    }

    private void filterIPS() {
        List<String[]> r = new ArrayList<>();

        for (String[] IP : this.IPS) {
            Arrays.sort(IP);
            String token = StringUtils.join(IP, ';');
            if (this.executedIPS.contains(token))
                continue;
            boolean failed = false;
            for (String failedIP : this.failedIPS)
                if (token.contains(failedIP)) {
                    failed = true;
                    break;
                }
            if (!failed)
                r.add(IP);
        }
        this.IPS = r;
    }

    private Assertion test() throws InterruptedException {

        int sampleBatch = 10;
        long duration = 30;
        int interval = 3;

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        // 当前测试的接口集合
        Set<String> urls = new HashSet<>();
        for (Trace t : this.currentTracePool)
            urls.addAll(Arrays.asList(t.getUrls()));

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 根据内置条件验证结果
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {
            Thread.sleep(interval * 1000);
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);

            for (Span root : spanTrees) {
                // 过滤无关trace
                if (!urls.contains(root.getUrl()))
                    continue;

                // 验证httpcode
                if (root.isErr() || Integer.valueOf(root.getCode()) >= 300)
                    return new Assertion("httpcode", root.getUrl(), "返回码为" + root.getCode());

                // 验证响应时间
                if (root.getDuration() / 1000 >= 10)
                    return new Assertion("responseTime", root.getUrl(), "响应时间为" + root.getDuration() / 1000);
            }
        }

        // 根据用户定义的验证脚本验证
        String params = StringUtils.join(urls.iterator(), ',');
        for (String assertionFile : this.configuration.getAssertionsUserDef()) {
            if (FileUtil.existFile(assertionFile)) {
                try {
                    //执行用户脚本
                    String str = Cmd.execForStd("python " + assertionFile + " -l " + params);

                    //验证结果
                    JsonArray results = new JsonParser().parse(str).getAsJsonArray();
                    if (results.size() == 0)
                        continue;
                    // [{"name":"...", "url":"...", "message": "..."}]
                    for (int i = 0; i < results.size(); ++i) {
                        JsonObject r = results.get(i).getAsJsonObject();
                        String url = r.get("url").getAsString();
                        if (urls.contains(url))
                            return new Assertion(r.get("name").getAsString(),
                                    url, r.get("message").getAsString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("用户上传的验证脚本有问题");
                }
            }
        }

        return null;
    }

    private void injectDelay(int duration) throws InterruptedException {
        Injector injector = new Injector();
        for (String service : this.currentIP) {
            Fault delay = new Delay(service, "v1", 100, duration);
            injector.inject(delay);

            this.currentFaults.add(delay);
        }
        Thread.sleep(5 * 1000);
    }

    private void injectAbort(int code) throws InterruptedException {
        Injector injector = new Injector();
        for (String service : this.currentIP) {
            Fault abort = new Abort(service, "v1", 100, code);
            injector.inject(abort);

            this.currentFaults.add(abort);
        }
        Thread.sleep(5 * 1000);
    }

    private void deleteFaults() throws InterruptedException {
        Injector injector = new Injector();
        for (Fault fault : this.currentFaults)
            injector.delete(fault);
        this.currentFaults = new ArrayList<>();
        Thread.sleep(5 * 1000);
    }

}
