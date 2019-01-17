package com.iscas.entity;

import java.util.List;
import java.util.Set;

public class Graph {

    private Set<String> nodes;
    private List<Edge> edges;

    public Graph(Set<String> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}
