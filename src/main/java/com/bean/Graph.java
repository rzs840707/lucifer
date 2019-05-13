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

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(List<Pair<String, String>> edges) {
        this.edges = edges;
    }
}
