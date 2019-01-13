package com.iscas.strategy;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PageRank {

    @Value("${pageRank.epoch}")
    private int MAX_EPOCH;

    // context
    private Map<String, Double> rank;
    private Map<String, Set<String>> graph;

    public void init(Map<String, Double> svcRank, Map<String, Set<String>> graph) {
        this.graph = graph;

        // init first generation rank
        rank = new HashMap<>(svcRank);

        // 1 for new svc
        for (String svc : graph.keySet())
            if (!rank.containsKey(svc))
                rank.put(svc, 1.0);
    }

    public Map<String, Double> iterate() {
        for (int generation = 0; generation < MAX_EPOCH; ++generation) {
            Map<String, Double> rankCopy = new HashMap<>(rank);
            for (String src : graph.keySet()) {
                Set<String> tars = graph.get(src);
                for (String tar : tars) {
                    double score = rank.get(src) / tars.size();
                    rankCopy.put(tar, score);
                }
            }
            rank = rankCopy;
        }
        return rank;
    }
}
