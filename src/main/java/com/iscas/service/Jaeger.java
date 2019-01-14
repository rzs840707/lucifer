package com.iscas.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iscas.bean.Span;
import com.iscas.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class Jaeger implements TraceTracker {

    private String host = "127.0.0.1:31211";

    private RestTemplate restTemplate;

    @Autowired
    public Jaeger(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Span> sample(int num, int lookback) {
        // 构造参数
        HashMap<String, String> params = new HashMap<>();
        params.put("service", "productpage");
        params.put("limit", String.valueOf(num));
//        params.put("lookback",lookback);
        long curTS = Time.getCurTimeStampMs() * 1000;
        params.put("end", String.valueOf(curTS));
        params.put("start", String.valueOf(curTS - lookback * 1000 * 1000));

        //查询并反序列化
        String str = query(params);
        JsonObject result = new JsonParser().parse(str).getAsJsonObject();
        if (!result.get("errors").isJsonNull()) {
            System.out.println(result.get("errors"));
            return new ArrayList<>();
        }
        return jsonToSpan(result.getAsJsonArray("data"));
    }

    @Override
    public List<Span> sampleErr(int num, int lookback) {
        // 构造参数
        HashMap<String, String> params = new HashMap<>();
        params.put("service", "productpage");
        params.put("limit", String.valueOf(num));
//        params.put("lookback",lookback);
        long curTS = Time.getCurTimeStampMs() * 1000;
        params.put("end", String.valueOf(curTS));
        params.put("start", String.valueOf(curTS - lookback * 1000 * 1000));
        params.put("tags", "{\"error\":\"true\"}");

        //查询并反序列化
        String str = query(params);
        JsonObject result = new JsonParser().parse(str).getAsJsonObject();
        if (!result.get("errors").isJsonNull()) {
            System.out.println(result.get("errors"));
            return new ArrayList<>();
        }
        return jsonToSpan(result.getAsJsonArray("data"));
    }

    private String query(Map<String, String> params) {
        String url = this.host + "/api/traces";

//        HttpHeaders headers = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder = builder.queryParam(entry.getKey(), entry.getValue());
        }
        HttpEntity<String> res = restTemplate.getForEntity(builder.build().encode().toUri(), String.class);
        return res.getBody();
    }

    private List<Span> jsonToSpan(JsonArray traces) {
        // 不包含istio工具的span
        Set<String> exName = new HashSet<>();
        exName.add("async outbound|9091||istio-policy.istio-system.svc.cluster.local egress");
        exName.add("kubernetes:handler.kubernetesenv.istio-system(kubernetesenv)");
        exName.add("/istio.mixer.v1.Mixer/Check");
        exName.add("Check");

        List<Span> trees = new ArrayList<>();
        for (JsonElement trace : traces) {
            JsonArray spans = trace.getAsJsonObject().getAsJsonArray("spans");

            // 收集所有span
            Map<Long, Span> spanMap = new HashMap<>();
            Map<Long, List<Long>> edges = new HashMap<>();
            for (JsonElement span : spans) {
                JsonObject spanInJson = span.getAsJsonObject();

                //获取参数并创建span
                String serviceName = spanInJson.get("operationName").getAsString();
                if (exName.contains(serviceName))
                    continue;
                long spanId = spanInJson.get("spanID").getAsLong();
                String timestamp = Time.timestamp2Str(spanInJson.get("startTime").getAsLong() / 1000);
                long duration = spanInJson.get("duration").getAsLong() / 1000;
                String method = null;
                String code = null;
                boolean error = false;
                String url = null;
                String kind = null;
                JsonArray tags = spanInJson.getAsJsonArray("tags");
                for (int i = 0; i < tags.size(); ++i) {
                    JsonObject tag = tags.get(i).getAsJsonObject();
                    String key = tag.get("key").getAsString();
                    switch (key) {
                        case "http.url":
                            url = tag.get("value").getAsString();
                            break;
                        case "http.method":
                            method = tag.get("value").getAsString();
                            break;
                        case "http.status_code":
                            code = tag.get("value").getAsString();
                            break;
                        case "error":
                            error = tag.get("value").getAsBoolean();
                            break;
                        case "span.kind":
                            kind = tag.get("value").getAsString();
                            break;
                    }
                }
                Span tmp = new Span(serviceName, url, method, code, duration, timestamp, error, kind);
                spanMap.put(spanId, tmp);

                // 构建边
                JsonArray father = spanInJson.getAsJsonArray("references");
                if (father.size() != 0) {
                    long fspanId = father.get(0).getAsJsonObject().get("spanID").getAsLong();
                    if (!edges.containsKey(fspanId))
                        edges.put(fspanId, new ArrayList<>());
                    edges.get(fspanId).add(spanId);
                }
            }

            // 构建树
            for (long fspanid : edges.keySet()) {
                List<Long> cspanids = edges.get(fspanid);
                Span fspan = spanMap.get(fspanid);
                Span[] cspans = new Span[cspanids.size()];
                fspan.setChildren(cspans);
                for (int i = 0; i < cspans.length; ++i) {
                    long cspanid = cspanids.get(i);
                    cspans[i] = spanMap.get(cspanid);
                }
            }

            // 调整树
            Span root = null;
            for (Span span : spanMap.values())
                if (span.getChildren().length == 0) {
                    root = span;
                    break;
                }
            LinkedList<Span> stack = new LinkedList<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                Span fspan = stack.pop();
                if (fspan.getChildren().length == 1) {
                    Span cspan = fspan.getChildren()[0];
                    if (cspan.getService().equals(fspan.getService())
                            && cspan.getKind().equals("server")
                            && fspan.getKind().equals("client")) {
                        // 合并代理调用被代理的服务
                        fspan.setChildren(cspan.getChildren());
                        fspan.setKind("server");
                        fspan.setTimestamp(cspan.getTimestamp());
                        fspan.setDuration(cspan.getDuration());
                    }
                }

                for (Span cspan : fspan.getChildren())
                    stack.push(cspan);
            }

            trees.add(root);
        }
        return trees;
    }
}
