package com.iscas.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iscas.bean.DataSeries;
import com.iscas.bean.Graph;
import com.iscas.util.Time;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class Telemetry {

    private RestTemplate restTemplate;

    private String host = "127.0.0.1:30795";

    @Autowired
    public Telemetry(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DataSeries[] query(String query) {
        String str = queryProm(query);
        JsonObject result = new JsonParser().parse(str).getAsJsonObject();
        if (!result.get("status").getAsString().equals("success")) {
            System.out.println(result.get("error"));
            return new DataSeries[0];
        }
        return deserializeProm(result.getAsJsonObject("data").getAsJsonArray("result"));
    }

    private String queryProm(String query) {
        String url = host + "/api/v1/query";

        //make up params
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("query", query);
        params.add("time", String.valueOf(Time.getCurTimeStampMs() / 1000));

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url).queryParams(params);
        HttpEntity<String> res = restTemplate.getForEntity(uriComponentsBuilder.build().encode().toUri(), String.class);
        return res.getBody();
    }

    private DataSeries[] deserializeProm(JsonArray series) {
        DataSeries[] result = new DataSeries[series.size()];
        for (int i = 0; i < result.length; ++i) {
            JsonObject dataSerie = series.get(i).getAsJsonObject();

            //获取tags
            JsonObject tmp = dataSerie.getAsJsonObject("metric");
            Map<String, String> tags = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : tmp.entrySet())
                tags.put(entry.getKey(), entry.getValue().getAsString());

            //获取值
            JsonArray tmp1 = dataSerie.getAsJsonArray("value");
            List<Pair<Double, String>> values = new ArrayList<>();
            for (int j = 1; j < tmp1.size(); j = j + 2) {
                Pair<Double, String> value = new Pair<>(tmp1.get(j - 1).getAsDouble(), tmp1.get(j).getAsString());
                values.add(value);
            }

            result[i] = new DataSeries(tags, values);
        }
        return result;
    }

    public Graph queryGraph(int lookback) {
        String queryUrl = host + "/d3graph?filter_empty=true&&time_horizon=" + lookback + "s";
        String res = restTemplate.getForEntity(queryUrl, String.class).getBody();
        JsonObject graph = new JsonParser().parse(res).getAsJsonObject();

        Set<String> exName = new HashSet<>();
        exName.add("unknown (unknown)");
        exName.add("telemetry (istio-telemetry)");
        exName.add("policy (istio-policy)");

        List<String> nodes = new ArrayList<>();
        List<Pair<String, String>> edges = new ArrayList<>();

        // 收集所有服务名称
        for (JsonElement node : graph.getAsJsonArray("nodes")) {
            String svc = node.getAsJsonObject().get("name").getAsString();
            if (!exName.contains(svc))
                nodes.add(svc.substring(0, svc.indexOf(' ')));
        }

        // 收集所有边
        JsonArray nodesArray = graph.getAsJsonArray("nodes");
        for (JsonElement edge : graph.getAsJsonArray("links")) {
            JsonObject e = edge.getAsJsonObject();
            int srcId = e.get("source").getAsInt();
            int tarId = e.get("target").getAsInt();
            String src = nodesArray.get(srcId).getAsJsonObject().get("name").getAsString();
            String tar = nodesArray.get(tarId).getAsJsonObject().get("name").getAsString();
            if (exName.contains(src) || exName.contains(tar))
                continue;

            src = src.substring(0, src.indexOf(' '));
            tar = tar.substring(0, tar.indexOf(' '));
            edges.add(new Pair<>(src, tar));
        }

        return new Graph(nodes, edges);

    }

}
