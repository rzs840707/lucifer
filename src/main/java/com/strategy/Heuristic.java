package com.iscas.strategy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iscas.bean.*;
import com.iscas.bean.fault.Abort;
import com.iscas.bean.fault.Delay;
import com.iscas.bean.fault.Fault;
import com.iscas.bean.result.*;
import com.iscas.dao.*;
import com.iscas.service.Injector;
import com.iscas.service.Jaeger;
import com.iscas.service.Telemetry;
import com.iscas.service.TraceTracker;
import com.iscas.util.Cmd;
import com.iscas.util.FileUtil;
import com.iscas.util.Kmp;
import com.iscas.util.Time;
import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Heuristic {

    public static int times = 0;

    @Autowired
    public Heuristic(Configuration configuration, SimpMessagingTemplate simpMessagingTemplate, RestTemplate restTemplate,
                     DetectResultDAO detectResultDAO, CircuitBreakerResultDAO circuitBreakerResultDAO,
                     BulkHeadResultDAO bulkHeadResultDAO, RetryResultDAO retryResultDAO,
                     TimeoutResultDAO timeoutResultDAO, SummaryDAO summaryDAO, InjectResultDAO injectResultDAO) {
        this.configuration = configuration;
        this.logPusher = simpMessagingTemplate;
        this.restTemplate = restTemplate;
        this.detectResultDAO = detectResultDAO;
        this.circuitBreakerResultDAO = circuitBreakerResultDAO;
        this.bulkHeadResultDAO = bulkHeadResultDAO;
        this.retryResultDAO = retryResultDAO;
        this.timeoutResultDAO = timeoutResultDAO;
        this.summaryDAO = summaryDAO;
        this.injectResultDAO = injectResultDAO;
    }

    // configuration
    private Configuration configuration;
    private boolean debug = false;

    // context
    private String id;
    private String startTime;
    private volatile List<Trace> tracePool;
    private volatile List<Trace> currentTracePool;
    private volatile Set<String> currentUrls;
    private volatile List<String[]> IPS;
    private volatile String[] currentIP;
    private List<Fault> currentFaults;
    private int injectTimes;
    private List<DetectResult> rs;
    private List<InjectResult> irs;
    private volatile List<TimeoutResult> tors;
    private volatile List<CircuitBreakResult> cbrs;
    private volatile List<RetryResult> rrs;
    private volatile List<BulkHeadResult> bhrs;
    private Set<String> executedIPS;
    private Set<String> failedIPS;
    private volatile boolean stop;

    // utils
    private Logger logger;
    private SimpMessagingTemplate logPusher;
    private RestTemplate restTemplate;
    private DetectResultDAO detectResultDAO;
    private CircuitBreakerResultDAO circuitBreakerResultDAO;
    private BulkHeadResultDAO bulkHeadResultDAO;
    private RetryResultDAO retryResultDAO;
    private TimeoutResultDAO timeoutResultDAO;
    private SummaryDAO summaryDAO;
    private InjectResultDAO injectResultDAO;

    public void run() {
        pushLog("自动探测流程开始");
        System.out.println("自动探测流程开始");
        init();
        this.log("参数初始化完毕");

        try {
            //抽样
            this.log("抽样正常调用链");
            sampleTraces();
            this.log("调用链抽样完毕");

            if (stop)
                return;

            // 探测
            this.log("开始对每个trace探测");
            for (Trace t : this.tracePool) {
                if (stop)
                    break;

                this.currentTracePool = new ArrayList<>();
                this.currentTracePool.add(t);
                this.log("当前tracepool为" + ArrayUtils.toString(t.getServices()));

                this.log("开始对当前的tracepool进行探测");
                detect();
                this.log("对当前tracepool探测结束");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.log("清理中");
        clean();
        System.out.println("清理完毕，探测流程完全结束");
        pushLog("清理完毕，探测流程完全结束");
    }

    private void init() {
        this.id = "detect_" + (++Heuristic.times) + "_" + Time.getCurTimeStr();

        // create logger
        LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender fileAppender = new FileAppender();
        fileAppender.setImmediateFlush(true);
        fileAppender.setContext(loggerContext);
        try {
            fileAppender.setFile(ResourceUtils.getFile("classpath:static/mylogs").getAbsolutePath() + "/"
                    + this.id + ".log");
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileAppender.setName(this.id + ".tmp");
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p 18737 --- [%t] %-10.10logger{10} : %m%n%wEx");
        encoder.start();
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        this.logger = loggerContext.getLogger(this.id);
        this.logger.addAppender(fileAppender);

        this.startTime = Time.getCurTimeStr();
        this.injectTimes = 0;
        this.currentFaults = new ArrayList<>();
        this.tors = new ArrayList<>();
        this.rrs = new ArrayList<>();
        this.cbrs = new ArrayList<>();
        this.bhrs = new ArrayList<>();
        this.rs = new ArrayList<>();
        this.irs = new ArrayList<>();
        this.executedIPS = new HashSet<>();
        this.failedIPS = new HashSet<>();
        this.stop = false;
    }

    private void detect() throws InterruptedException {
        while (true) {
            if (stop)
                return;

            // 求解IPS
            this.log("约束求解");
            this.IPS = new SAT().sat(this.currentTracePool);
            this.log("求解结果为:" + this.IPS2str());

            if (stop)
                break;

            //过滤IPS
            this.log("开始过滤IPS");
            filterIPS();
            this.log("结束过滤IPS");
            if (this.IPS.isEmpty()) {
                this.log("IPS为空，结束对于当前tracePool的探测");
                break;
            } else
                this.log("待测故障注入点集合为" + this.IPS2str());

            if (stop)
                return;

            // 更新当前的urls
            this.currentUrls = new HashSet<>();
            for (Trace t : this.currentTracePool)
                this.currentUrls.addAll(Arrays.asList(t.getUrls()));
            this.log("当前的接口集合为" + ArrayUtils.toString(this.currentUrls));

            if (stop)
                return;

            // 逐个探测
            this.log("开始对每个故障注入点探测");
            for (String[] IP : this.IPS) {
                if (stop)
                    return;

                this.currentIP = IP;
                this.injectTimes++;
                this.log("当前为第" + this.injectTimes + "次探测");

                // 标记已经执行过
                Arrays.sort(this.currentIP);
                String token = StringUtils.join(this.currentIP, ";");
                this.executedIPS.add(token);
                this.log("开始对" + token + "的探测");

                if (stop)
                    return;

                // 注入故障
                this.log("注入500丢包");
                injectAbort(500);
                this.log("注入完毕");

                if (stop)
                    return;

                // 测试响应
                this.log("验证断言");
                Assertion failedAssertion = test();
                this.log("验证完毕");

                if (stop)
                    return;

                // 更新
                if (failedAssertion == null) {
                    this.log("无异常，开始更新调用链");
                    updateTraces();
                    this.log("更新完毕，删除故障");
                    deleteFaults();
                    this.log("删除故障完毕");

                    // 记录本次注入的测试结果
                    generateInjectResult(true);
                }
                // 分析
                else {
                    this.rs.add(new DetectResult(this.id, this.injectTimes, failedAssertion.toString(), Time.getCurTimeStr()));
                    this.failedIPS.add(token);
                    generateInjectResult(false);

                    this.log("删除故障");
                    deleteFaults();
                    this.log("开始模式分析");
                    analysis();
                    this.log("模式分析结束");
                }
            }
            this.log("对当前IPS探测结束");
        }
    }

    private void generateInjectResult(boolean success) {
        String ip = StringUtils.join(this.currentIP, ',');
        String traceID = "";
        if (this.currentTracePool.size() > 0)
            traceID = currentTracePool.get(currentTracePool.size() - 1).getSpan().getTraceId();
        InjectResult ir = new InjectResult(this.id, this.injectTimes, ip, Time.getCurTimeStr(), success, traceID);
        this.irs.add(ir);
    }

    private void sampleTraces() throws InterruptedException {
        int sampleBatch = this.configuration.getTraceDetectSampleBatch();
        long duration = this.configuration.getTraceDetectDuration();
        int interval = this.configuration.getTraceDetectSampleInterval();
        this.log("参数：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;
        Map<Integer, Trace> traces = new HashMap<>();
        TraceTracker tracker = new Jaeger(this.restTemplate);
        this.log("采样中");
        // sample traces
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {

            if (this.stop)
                break;

            this.log("等待" + interval + "+5秒,zipkin抓取中");
            if (!debug)
                Thread.sleep((interval + 5) * 1000);

            this.log("查询中");
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);

            this.log("解析中");
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

            // 测试过程中只执行一轮
            if (debug)
                break;
        }

        this.tracePool = new ArrayList<>(traces.values());
        this.log("共收集到" + this.tracePool.size() + "条不同trace");
    }

    private void updateTraces() throws InterruptedException {
        int sampleBatch = this.configuration.getTraceDetectSampleBatch();
        long duration = 30; // 抽样30秒
        int interval = this.configuration.getTraceDetectSampleInterval();

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // sample traces
        this.log("参数：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");
        Set<String> tokens = new HashSet<>(); // 已经添加的
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {
            if (!debug) {
                this.log("等待" + interval + "秒");
                Thread.sleep((interval) * 1000);
            }

            // sample
            this.log("抽样调用链");
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);

            this.log("分析");
            for (Span root : spanTrees) {
                Trace t = spanToTrace(root);
                String url = t.getUrls()[0];
                if (this.currentUrls.contains(url)) {
                    List<String> services = new ArrayList<>(Arrays.asList(t.getServices()));
                    for (String exService : this.currentIP)
                        services.remove(exService);

                    if (services.size() > 0) {
                        Collections.sort(services);
                        String token = StringUtils.join(services.iterator(), ';');
                        // 不添加重复的trace
                        if (tokens.contains(token))
                            continue;

                        // 添加新的trace
                        t.setServices(services.toArray(new String[0]));
                        this.currentTracePool.add(t);
                        tokens.add(token);
                        this.log("加入新的trace" + ArrayUtils.toString(t.getServices()));
                    }
                }
            }

            if (debug)
                break;
        }
    }

    private void analysis() throws InterruptedException {
        if (stop)
            return;
        this.log("开始超时模式分析");
        analysisTimeout();
        this.log("超时模式分析结束");
        if (stop)
            return;
        this.log("开始重试模式分析");
        analysisRery();
        this.log("重试模式分析结束");
        if (stop)
            return;
        this.log("开始船舱模式分析");
        analysisBulkHead();
        this.log("船舱模式分析结束");
        if (stop)
            return;
        this.log("开始熔断模式分析");
        analysisCircuitBreaker();
        this.log("熔断模式分析结束");
    }

    private Map<Pair<String, String>, Long> detectTimeout() {

        Map<Pair<String, String>, Long> result = new HashMap<>();

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 参数
        long duration = this.configuration.getTimeoutDetectionDuration();
        int interval = this.configuration.getTimeoutSampleInterval();
        int sampleBatch = this.configuration.getTimeoutSampleBatch();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        this.log("参数：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");
        // 探测
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            this.log("抽样");
            List<Span> roots = tracker.sampleErr(sampleBatch, interval);

            // 抽样
//            this.log("统计并计算结果");
            for (Span root : roots) {
                // 如果不是目标接口产生的，则跳过
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;

                // DFS
                LinkedList<Span> stack = new LinkedList<>();
                stack.push(root);
                while (!stack.isEmpty()) {
                    Span fatherSpan = stack.pop();
                    for (Span childSpan : fatherSpan.getChildren()) {
                        stack.push(childSpan);

                        Pair<String, String> servicePair = new Pair<>(fatherSpan.getService(), childSpan.getService());
                        result.put(servicePair, childSpan.getDuration());

//                        // 生成测试结果
//                        if (fatherSpan.getDuration() < childSpan.getDuration()) {
//                            this.log("探测到异常");
//                            TimeoutResult rs = new TimeoutResult(this.id, this.injectTimes,
//                                    getServiceName(fatherSpan.getService()),
//                                    getServiceName(childSpan.getService())
//                                    , fatherSpan.getDuration(),
//                                    "上游服务阈值（" + fatherSpan.getDuration() + "）小于下游响应时间（" + childSpan.getDuration() + "）", Time.getCurTimeStr());
//                            this.tors.add(rs);
//                        }
                    }
                }
            }
        }
        return result;
    }

    private void analysisTimeout() throws InterruptedException {
//        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 探测正常响应时间
        this.log("抽样正常响应时间");
        Map<Pair<String, String>, Long> times1 = detectTimeout();

        // 注入delay故障
        this.log("注入20秒超时");
        this.injectDelay(20);

        // 抽样异常响应时间
        this.log("抽样异常响应时间");
        Map<Pair<String, String>, Long> times2 = detectTimeout();

        for (Pair<String, String> servicePair : times2.keySet()) {
            long time1 = times1.getOrDefault(servicePair, 0L);
            long time2 = times2.get(servicePair);
            if (time2 - time1 > 1000) {
                TimeoutResult rs = new TimeoutResult(this.id, this.injectTimes,
                        getServiceName(servicePair.getKey()),
                        getServiceName(servicePair.getValue())
                        , time2, "正常响应时间为" + time1, Time.getCurTimeStr());
                this.tors.add(rs);
            }
        }

//        // 参数
//        long duration = this.configuration.getTimeoutDetectionDuration();
//        int interval = this.configuration.getTimeoutSampleInterval();
//        int sampleBatch = this.configuration.getTimeoutSampleBatch();
//        long startTime = Time.getCurTimeStampMs();
//        long endTime = startTime + duration * 1000;
//
//        this.log("参数：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");
//        // 探测
//        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
//            // 暂停一段时间
//            if (!debug) {
//                this.log("等待" + interval + "秒");
//                Thread.sleep((interval) * 1000);
//
//            }
//            this.log("抽样");
//            List<Span> roots = tracker.sampleErr(sampleBatch, interval);
//
//            // 抽样
//            this.log("统计并计算结果");
//            for (Span root : roots) {
//                // 如果不是目标接口产生的，则跳过
//                if (!this.currentUrls.contains(root.getUrl()))
//                    continue;
//
//                // DFS
//                LinkedList<Span> stack = new LinkedList<>();
//                stack.push(root);
//                while (!stack.isEmpty()) {
//                    Span fatherSpan = stack.pop();
//                    for (Span childSpan : fatherSpan.getChildren()) {
//                        stack.push(childSpan);
//
//                        // 生成测试结果
//                        if (fatherSpan.getDuration() < childSpan.getDuration()) {
//                            this.log("探测到异常");
//                            TimeoutResult rs = new TimeoutResult(this.id, this.injectTimes,
//                                    getServiceName(fatherSpan.getService()),
//                                    getServiceName(childSpan.getService())
//                                    , fatherSpan.getDuration(),
//                                    "上游服务阈值（" + fatherSpan.getDuration() + "）小于下游响应时间（" + childSpan.getDuration() + "）", Time.getCurTimeStr());
//                            this.tors.add(rs);
//                        }
//                    }
//                }
//            }
//            // 测试环境只测试一轮抽样
//            if (debug)
//                break;
//
//        }

//        // 诊断
//        System.out.println("诊断");
//        Map<String, Double> responseTimeByFather = new HashMap<>();
//        for (Pair<String, String> servicePair : responseTimeTotal.keySet()) {
//            String father = servicePair.getKey();
//            if (!responseTimeByFather.containsKey(father))
//                responseTimeByFather.put(father, 0.0);
//            responseTimeByFather.put(father, responseTimeByFather.get(father) + responseTimeTotal.get(servicePair));
//        }
//
//        // 生成结果
//        System.out.println("生成结果");
//        for (Pair<String, String> servicePair : responseTimeTotal.keySet()) {
//            String message = "无异常";
//            if (responseTimeTotal.get(servicePair) <= responseTimeByFather.get(servicePair.getKey()))
//                message = servicePair.getKey() + "阈值过小";
//            TimeoutResult rs = new TimeoutResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue()
//                    , responseTimeTotal.get(servicePair), message, Time.getCurTimeStr());
//            this.tors.add(rs);
//        }

        // 删除故障
        System.out.println("删除故障");
        this.deleteFaults();
    }

    private void analysisCircuitBreaker() throws InterruptedException {
        TraceTracker tracker = new Jaeger(this.restTemplate);
        Span normalSpanTree = null;

        int maxtry = 100;

        // 对正常情况下调用链采样
        this.log("对正常情况下链采样");
        while (normalSpanTree == null) {

            if (!debug) {
                this.log("等待10秒");
                Thread.sleep(10 * 1000);
            }

            this.log("采样");
            List<Span> roots = tracker.sample(10, 10);
            for (Span root : roots)
                if (this.currentUrls.contains(root.getUrl())) {
                    normalSpanTree = root;
                    break;
                }

            // 始终无法抽取到目标接口的trace
            maxtry--;
            if (maxtry == 0) {
                this.log("一直未抽取到目标接口的trace，熔断探测结束");
                return;
            }
        }
        this.log("获取正常链完毕");

        // 获得所有的边
        this.log("解析正常链中所有的边");
        Map<String, Set<String>> edges = new HashMap<>();
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
        this.log("注入故障");
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

        // 探测熔断位置
        this.log("开始探测熔断位置");
        this.log("参数：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");
        Set<Pair<String, String>> locations = new HashSet<>();
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {

            List<Span> roots = tracker.sample(sampleBatch, interval);

            //暂停一段时间
            if (!debug) {
                this.log("等待" + interval + "秒");
                Thread.sleep((interval) * 1000);
            }

            this.log("分析熔断位置");
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
                    for (String childService : tmp) {
                        String name1 = span.getService().substring(0, span.getService().indexOf('.'));
                        String name2 = childService.substring(0, childService.indexOf('.'));
                        int oriSize = locations.size();
                        locations.add(new Pair<>(name1, name2));
                        if (locations.size() > oriSize)
                            this.log("探测到熔断位置：" + name1 + " -> " + name2);
                    }
                }
            }

            if (debug)
                break;
        }

        // 缺失熔断
        if (locations.isEmpty()) {
            this.log("缺失熔断");
            this.cbrs.add(new CircuitBreakResult(this.id, this.injectTimes,
                    null, null, 0.0,
                    "缺失熔断,故障位置:" + ArrayUtils.toString(this.currentIP, ","),
                    Time.getCurTimeStr(), this.currentUrls.toArray(new String[0])));
        } else {
            this.log("开始监控熔断位置的吞吐量");
            // 监控半开状态
            startTime = Time.getCurTimeStampMs();
            endTime = startTime + duration2 * 1000;
            this.log("参数：持续" + duration2 + "秒，间隔为" + interval2 + "秒，窗口大小为" + windowSize + "秒");

            Telemetry monitor = new Telemetry(this.restTemplate);
            Map<Pair<String, String>, Double> result = new HashMap<>();
            //探测半开状态
            for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {

                // 暂停一段时间
                if (!debug) {
                    System.out.println("等待" + interval2 + "秒");
                    Thread.sleep((interval) * 1000);
                }

                this.log("查询吞吐量数据");
                String query = "sum(irate(istio_requests_total{reporter=\"source\"}[" + windowSize
                        + "s])) by (source_app,destination_service_name)";
                DataSeries[] dataSeries = monitor.query(query);

                this.log("解析吞吐量数据");
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

                if (debug)
                    break;
            }

            // 生成探测结果
            this.log("生成测试结果");
            for (Pair<String, String> servicePair : result.keySet())
                this.cbrs.add(new CircuitBreakResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue(),
                        result.get(servicePair), "熔断被探测到", Time.getCurTimeStr(), this.currentUrls.toArray(new String[0])));
        }

        //删除故障
        this.log("删除故障");
        this.deleteFaults();
    }

    private void analysisRery() throws InterruptedException {
        TraceTracker tracker = new Jaeger(this.restTemplate);
        // 参数
        long duration = this.configuration.getRetryDetectionDuration();
        int interval = this.configuration.getRetrySampleInterval();
        int sampleBatch = this.configuration.getRetrySampleBatch();
        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        // 抽样正常请求
        this.log("抽样正常请求");
        // 暂停一段时间
        if (!debug) {
            this.log("等待" + interval + "秒");
            Thread.sleep((interval) * 1000);
        }

        this.log("抽样中");
        List<Span> roots = tracker.sample(sampleBatch, interval);

        this.log("抽取正常请求数量");
        Map<Pair<String, String>, Integer> normalCntr = new HashMap<>();
        for (Span root : roots) {
            LinkedList<Span> stack = new LinkedList<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                Span fspan = stack.pop();
                Map<String, Integer> tmp = new HashMap<>();
                for (Span cspan : fspan.getChildren()) {
                    //只收集和遍历非异常子树的内容
                    if (!cspan.isErr()) {
                        String cname = cspan.getService();
                        tmp.put(cname, tmp.getOrDefault(cname, 0) + 1);
                        stack.push(cspan);
                    }
                }
                for (String cname : tmp.keySet())
                    normalCntr.put(new Pair<>(fspan.getService(), cname), tmp.get(cname));
            }
        }


        // 注入delay故障
        this.log("注入丢包");
        this.injectAbort(500);
        this.log("注入完毕");

        // 探测
        this.log("开始探测，参数为：持续" + duration + "秒，间隔为" + interval + "秒，抽样数量为" + sampleBatch + "条");
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            // 暂停一段时间
            if (!debug) {
                this.log("等待" + interval + "秒");
                Thread.sleep((interval) * 1000);
            }

            this.log("抽样故障调用链中");
            roots = tracker.sampleErr(sampleBatch, interval);

            this.log("分析中");
            for (Span root : roots) {
                // 如果不是目标接口产生的，则跳过
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;

                LinkedList<Span> stack = new LinkedList<>();
                stack.push(root);
                while (!stack.isEmpty()) {
                    Span fatherSpan = stack.pop();
                    Map<String, Integer> counter = new HashMap<>();
                    for (Span childSpan : fatherSpan.getChildren()) {
                        stack.push(childSpan);

                        if (childSpan.isErr()) {
                            String name = childSpan.getService();
                            if (!counter.containsKey(name))
                                counter.put(name, 0);
                            counter.put(name, counter.get(name) + 1);
                        }
                    }

                    //收集结果
                    for (String target : counter.keySet()) {
                        int normalTimes = normalCntr.getOrDefault(new Pair<>(fatherSpan.getService(), target), 1);
                        if (counter.get(target) != normalTimes) {
                            this.log("探测到异常重试情况");
                            this.rrs.add(new RetryResult(this.id, this.injectTimes, root.getUrl(),
                                    getServiceName(fatherSpan.getService()),
                                    getServiceName(target),
                                    counter.get(target),
                                    "原请求次数为" + normalTimes + ",重试次数为" + counter.get(target),
                                    Time.getCurTimeStr()));
                        }
                    }
                }

            }

            // 测试环境只测试一轮抽样
            if (debug)
                break;

        }

        // 删除故障
        this.log("删除故障");
        this.deleteFaults();
        this.log("故障删除完毕");
    }

    private void analysisBulkHead() throws InterruptedException {

        this.log("查询兄弟节点");
        if (!debug) {
            this.log("等待30秒");
            Thread.sleep(30 * 1000);
        }

        // 确定注入点的父节点与兄弟节点所构成的边集合
        this.log("查询图");
        Graph graph = new Telemetry(this.restTemplate).queryGraph(30);
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
                fatherServices.add(edge.getKey());
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

        this.log("兄弟节点共" + links.size() + "个");
        if (links.isEmpty()) {
            this.log("不存在兄弟节点,结束船舱模式的探测");
            return;
        }

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
        this.log("探测父节点到兄弟节点的正常吞吐量");
        this.log("开始探测，参数为：持续" + duration1 + "秒，间隔为" + interval1 + "秒，窗口为" + windowSize1 + "秒");
        // 获取正常数据
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            if (!debug) {
                this.log("等待" + interval1 + "秒");
                Thread.sleep(interval1 * 1000);
            }

            this.log("查询吞吐量");
            String query = "sum(irate(istio_requests_total{reporter=\"source\"}[" + windowSize1
                    + "s])) by (source_app,destination_service_name)";
            DataSeries[] dataSeries = monitor.query(query);

            this.log("获取吞吐量");
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
                        normal.put(servicePair, Math.max(normal.get(servicePair), tmp));
                }
            }

            if (debug)
                break;
        }
        this.log("正常吞吐量探测完毕");

        // 注入故障
        this.log("注入20秒延迟");
        this.injectDelay(20);

        // 检测
        Map<Pair<String, String>, Double> result = new HashMap<>();
        startTime = Time.getCurTimeStampMs();
        endTime = startTime + duration2 * 1000;

        this.log("探测父节点到兄弟节点的异常吞吐量");
        this.log("开始探测，参数为：持续" + duration2 + "秒，间隔为" + interval2 + "秒，窗口为" + windowSize2 + "秒");
        for (long currentTime = Time.getCurTimeStampMs(); currentTime < endTime; currentTime = Time.getCurTimeStampMs()) {
            //暂停一段时间
            if (!debug) {
                this.log("等待" + interval2 + "秒");
                Thread.sleep(interval2 * 1000);
            }

            this.log("查询吞吐量");
            String query = "sum(irate(istio_requests_total{reporter=\"source\"}[" + windowSize2
                    + "s])) by (source_app,destination_service_name)";
            DataSeries[] dataSeries = monitor.query(query);

            this.log("获取吞吐量");
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

            if (debug)
                break;
        }
        this.log("异常吞吐量探测完毕");

        // 记录结果
        this.log("分析结果");
        for (Pair<String, String> servicePair : result.keySet()) {
            this.log("探测到船舱模式缺失");
            this.bhrs.add(new BulkHeadResult(this.id, this.injectTimes, servicePair.getKey(), servicePair.getValue(),
                    normal.get(servicePair), result.get(servicePair), "缺失资源隔离", Time.getCurTimeStr()));
        }

        //删除故障
        this.log("删除故障");
        this.deleteFaults();
    }

    private void clean() {
        this.logger.getAppender(this.id + ".tmp").stop();
        this.logger = null;

        pushLog("持久化中");
        this.summaryDAO.save(new Summary(this.id, this.startTime, Time.getCurTimeStr(), this.injectTimes, this.injectTimes * 4,
                this.tors.size(), this.cbrs.size(), this.bhrs.size(), this.rrs.size()));

        // 保存所有结果
        pushLog("保存探测结果");
        this.detectResultDAO.saveAll(this.rs);
        // 注入记录
        pushLog("保存注入记录");
        this.injectResultDAO.saveAll(this.irs);
        pushLog("保存超时结果");
        this.timeoutResultDAO.saveAll(this.tors);
        pushLog("保存重试结果");
        this.retryResultDAO.saveAll(this.rrs);
        pushLog("保存船舱结果");
        this.bulkHeadResultDAO.saveAll(this.bhrs);
        pushLog("保存熔断结果");
        this.circuitBreakerResultDAO.saveAll(this.cbrs);
    }

    private void pushLog(String log) {
        String topic = "/detection/operation";
        logPusher.convertAndSend(topic, Time.getCurTimeStr(Time.pattern1) + " " + log);
    }

    private void log(String msg) {
        // 输出到文件
        this.logger.info(msg);
        // 输出到web
        pushLog(msg);
        // 输出到控制台
        System.out.println(msg);
    }

    private Trace spanToTrace(Span root) {
        LinkedList<Span> stack = new LinkedList<>();
        String url = root.getUrl();
        Set<String> services = new HashSet<>();

        // add root
        services.add(getServiceName(root.getService()));
        stack.push(root);

        // DFS
        while (!stack.isEmpty()) {
            Span fSpan = stack.pop();
            Span[] children = fSpan.getChildren();
            for (int idx = children.length - 1; idx >= 0; --idx) {
                Span child = children[idx];
                services.add(getServiceName(child.getService()));
                stack.push(child);
            }
        }

        return new Trace(new String[]{url}, services.toArray(new String[0]), root);
    }

    private String getServiceName(String complexName) {
        return complexName.substring(0, complexName.indexOf('.'));
    }

    private void filterIPS() {
        List<String[]> r = new ArrayList<>();

        for (String[] IP : this.IPS) {
            Arrays.sort(IP);
            String token = StringUtils.join(IP, ';');

            // 过滤包含已经执行的故障注入点
            if (this.executedIPS.contains(token)) {
                this.log(ArrayUtils.toString(IP) + "已经执行过，被过滤");
                continue;
            }

            // 过滤失败的故障注入点
            boolean failed = false;
            for (String failedIP : this.failedIPS)
                if (Kmp.KMP(token, failedIP) == 0) {
                    failed = true;
                    break;
                }
//            boolean failed = false;
//            for (String failedIP : this.failedIPS)
//                if (token.contains(failedIP)) {
//                    failed = true;
//                    break;
//                }
            if (!failed)
                r.add(IP);
            else
                this.log(ArrayUtils.toString(IP) + "包含实效的故障注入点，被过滤");
        }
        this.IPS = r;
    }

    private String IPS2str() {
        StringBuilder sb = new StringBuilder();
        for (String[] tmp : this.IPS) {
            sb.append(ArrayUtils.toString(tmp));
        }
        return sb.toString();
    }

    private Assertion test() throws InterruptedException {
        int sampleBatch = 10;
        long duration = 10;
        int interval = 10;

        Assertion failed = null;

        long startTime = Time.getCurTimeStampMs();
        long endTime = startTime + duration * 1000;

        TraceTracker tracker = new Jaeger(this.restTemplate);

        // 根据内置条件验证结果
        this.log("验证返回结果和响应时间");
        int total = 0;
        int err = 0;
        for (long curTime = Time.getCurTimeStampMs(); curTime < endTime; curTime = Time.getCurTimeStampMs()) {

            if (!debug) {
                this.log("等待" + interval + "秒");
                Thread.sleep((interval) * 1000);
            }

            this.log("采样调用链");
            List<Span> spanTrees = tracker.sample(sampleBatch, interval);

            this.log("逐个trace验证");
            for (Span root : spanTrees) {
                // 过滤无关trace
                if (!this.currentUrls.contains(root.getUrl()))
                    continue;
                total += 1;

                // 验证httpcode
                if (root.isErr() || Integer.valueOf(root.getCode()) >= 300) {
                    err += 1;
                    failed = new Assertion("httpcode", root.getUrl(), "返回码为" + root.getCode());
                }


                // 验证响应时间
                if (root.getDuration() / 1000 >= 10) {
                    err += 1;
                    failed = new Assertion("responseTime", root.getUrl(), "响应时间为" + root.getDuration() / 1000);
                }
            }

            if (debug)
                break;
        }

        if (err > total / 2) {
            this.pushLog(failed.toString());
            return failed;
        } else {
            total = 0;
            err = 0;
            this.pushLog("返回结果和响应时间正常");
        }

        // 根据用户定义的验证脚本验证
        this.log("验证用户自定义脚本");
        String params = StringUtils.join(this.currentUrls.iterator(), ' ');
        for (total = 0; total <= 10; total++) {
            for (String assertionFile : this.configuration.getAssertionsUserDef()) {
                if (FileUtil.existFile(assertionFile)) {
                    this.pushLog("开始验证" + assertionFile);
                    try {
                        //执行用户脚本
                        String str = Cmd.execForStd("python " + assertionFile + " " + params);
                        if (str.isEmpty())
                            continue;

                        //验证结果
                        JsonArray results = new JsonParser().parse(str).getAsJsonArray();
                        if (results.size() == 0)
                            continue;
                        // [{"name":"...", "url":"...", "message": "..."}]
                        for (int i = 0; i < results.size(); ++i) {
                            JsonObject r = results.get(i).getAsJsonObject();
                            String url = r.get("url").getAsString();
                            if (this.currentUrls.contains(url)) {
                                err += 1;
                                failed = new Assertion(r.get("name").getAsString(),
                                        url, r.get("message").getAsString());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.pushLog(assertionFile + "有问题");
                    }
                }
            }
        }
        if (err > total / 2) {
            this.pushLog(failed.toString());
            return failed;
        }
        this.pushLog("返回结果和响应时间正常");
        return null;
    }

    private void injectDelay(int duration) throws InterruptedException {
        Injector injector = new Injector();
        for (String service : this.currentIP) {
            Fault delay = new Delay(service, "v1", 100, duration);
            injector.inject(delay);

            this.currentFaults.add(delay);
        }
        this.log("等待5秒");
        Thread.sleep(5 * 1000);
    }

    private void injectAbort(int code) throws InterruptedException {
        Injector injector = new Injector();
        for (String service : this.currentIP) {
            Fault abort = new Abort(service, "v1", 100, code);
            injector.inject(abort);

            this.currentFaults.add(abort);
        }
        this.log("等待5秒");
        Thread.sleep(5 * 1000);
    }

    private void deleteFaults() throws InterruptedException {
        Injector injector = new Injector();
        for (Fault fault : this.currentFaults) {
            injector.delete(fault);
//            injector.deleteVirtualservice(fault.getTarService());
        }
        this.currentFaults = new ArrayList<>();

        this.log("等待5秒");
        Thread.sleep(5 * 1000);
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public List<TimeoutResult> getTors() {
        return tors;
    }

    public List<CircuitBreakResult> getCbrs() {
        return cbrs;
    }

    public List<RetryResult> getRrs() {
        return rrs;
    }

    public List<BulkHeadResult> getBhrs() {
        return bhrs;
    }

    public List<Trace> getTracePool() {
        return tracePool;
    }

    public List<Trace> getCurrentTracePool() {
        return currentTracePool;
    }

    public Set<String> getCurrentUrls() {
        return currentUrls;
    }

    public List<String[]> getIPS() {
        return IPS;
    }

    public String getId() {
        return id;
    }

    public String[] getCurrentIP() {
        return currentIP;
    }
}
