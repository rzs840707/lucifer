package com.iscas.bean.result;

import com.iscas.bean.Trace;
import com.iscas.bean.fault.Fault;

import java.util.List;

public class CircuitBreakResult {
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double halfthroughout;
    private String message;
    private String timestamp;
    private String[] urls;

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
}
