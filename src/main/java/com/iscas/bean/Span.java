package com.iscas.bean;

import javafx.util.Pair;

import java.util.List;
import java.util.Set;

public class Span {
    private String id;
    private String service;
    private String url;
    private String method;
    private String protocol;
    private String code;
    private long duration; // ms
    private String timestamp;
    private boolean err;
    private Span[] children;

    public Span(String service, String url, String method, String protocol, String code, long duration, String timestamp, boolean err) {
        this.service = service;
        this.url = url;
        this.method = method;
        this.protocol = protocol;
        this.code = code;
        this.duration = duration;
        this.timestamp = timestamp;
        this.err = err;
    }

    public String getUrl() {
        return url;
    }

    public Span[] getChildren() {
        return children;
    }

    public String getService() {
        return service;
    }

    public boolean isErr() {
        return err;
    }

    public long getDuration() {
        return duration;
    }
}