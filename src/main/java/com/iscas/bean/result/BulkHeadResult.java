package com.iscas.bean.result;

import com.iscas.bean.fault.Fault;

public class BulkHeadResult {
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double normalthroughout;
    private double worstThroughout;
    private String message;
    private String timestamp;

    public BulkHeadResult(String id, int injectTime, String srcName,
                          String targetName, double normalthroughout, double worstThroughout, String message, String timestamp) {
        this.id = id;
        this.injectTime = injectTime;
        this.srcName = srcName;
        this.targetName = targetName;
        this.normalthroughout = normalthroughout;
        this.worstThroughout = worstThroughout;
        this.message = message;
        this.timestamp = timestamp;
    }
}
