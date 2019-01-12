package com.iscas.bean;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public class DataSeries {
    private Map<String, String> tags;
    private List<Pair<Double, String>> data;

    public DataSeries(Map<String, String> tags, List<Pair<Double, String>> data) {
        this.tags = tags;
        this.data = data;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public List<Pair<Double, String>> getData() {
        return data;
    }
}
