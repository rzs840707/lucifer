package com.iscas.bean.result;

public class TimeoutResult {
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double threshold;
    private String message;
    private String timestamp;

    public TimeoutResult(String id, int injectTime, String srcName,
                         String targetName, double threshold, String message, String timestamp) {
        this.id = id;
        this.injectTime = injectTime;
        this.srcName = srcName;
        this.targetName = targetName;
        this.threshold = threshold;
        this.message = message;
        this.timestamp = timestamp;
    }
}
