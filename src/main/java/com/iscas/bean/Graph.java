package com.iscas.bean;

import javafx.util.Pair;

import java.util.List;

public class Graph {
    private List<String> nodes;
    private List<Pair<String, String>> edges;

    public Graph(List<String> nodes, List<Pair<String, String>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Pair<String, String>> getEdges() {
        return edges;
    }
}
