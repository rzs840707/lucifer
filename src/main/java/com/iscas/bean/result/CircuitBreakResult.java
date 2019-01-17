package com.iscas.bean.result;

import org.apache.commons.lang3.ArrayUtils;

public class CircuitBreakResult {
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double halfthroughout;
    private String message;
    private String timestamp;
    private String[] urls;
    private String urlsStr;

    public CircuitBreakResult(String id, int injectTime, String srcName, String targetName,
                              double halfthroughout, String message, String timestamp, String[] urls) {
        this.id = id;
        this.injectTime = injectTime;
        this.srcName = srcName;
        this.targetName = targetName;
        this.halfthroughout = halfthroughout;
        this.message = message;
        this.timestamp = timestamp;
        this.urls = urls;
    }

    public String getId() {
        return id;
    }

    public int getInjectTime() {
        return injectTime;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getTargetName() {
        return targetName;
    }

    public double getHalfthroughout() {
        return halfthroughout;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String[] getUrls() {
        return urls;
    }

    public String getUrlsStr() {
        return ArrayUtils.toString(urls, ",");
    }

    public void setUrlsStr(String urlsStr) {
        this.urlsStr = urlsStr;
    }
}
