package com.iscas.bean;

public class Span {
    private String service;
    private String url;
    private String method;
    private String code;
    private long duration; // 毫秒
    private String timestamp;
    private boolean err;
    private Span[] children;
    private String kind;

    public Span(String service, String url, String method, String code, long duration, String timestamp, boolean err, String kind) {
        this.service = service;
        this.url = url;
        this.method = method;
        this.code = code;
        this.duration = duration;
        this.timestamp = timestamp;
        this.err = err;
        this.children = new Span[0];
        this.kind = kind;
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

    /**
     *
     * @return 毫秒级别
     */
    public long getDuration() {
        return duration;
    }

    public void setChildren(Span[] children) {
        this.children = children;
    }

    public String getKind() {
        return kind;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }
}