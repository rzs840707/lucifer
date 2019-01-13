package com.iscas.bean;

import com.iscas.bean.assertion.Assertion;

import java.util.List;

public class Configuration {
    // service scope
    String[] services;

    // trace detection
    private long traceDetectDuration; // seconds
    private int traceDetectSampleBatch;
    private int traceDetectSampleInterval; // seconds

    // assertions
    private List<Assertion> assertions; // 用户响应是否正常的判断依据
    private String assertionsUserDef; // 用户响应是否正常的判断依据(用户定义的python文件)

    // timeout detection
    private long timeoutDetectionDuration; // seconds
    private int timeoutSampleInterval; // seconds
    private int timeoutSampleBatch;

    // retry detection
    private long retryDetectionDuration; // seconds
    private int retrySampleInterval; // seconds
    private int retrySampleBatch;

    // circuit breaker detection
    private long circuitbreakerLocateDuration; //寻找熔断位置时长
    private int circuitbreakerLocateInterval; //seconds
    private int circuitbreakerLocateSampleBatch;
    private int circuitbreakerQPSWindowSize; // seconds
    private long circuitbreakerHalfStateDuration; // 监控半开状态时长
    private int circuitbreakerInterval;

    // bulkhead detection
    private long bulkheadDuration1; // seconds
    private int bulkheadWindowSize1;
    private int bulkheadInterval1;
    private long bulkheadDuration2; // seconds
    private int bulkheadWindowSize2;
    private int bulkheadInterval2;
    private double bulkheadThreshold;

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        this.services = services;
    }

    public long getTraceDetectDuration() {
        return traceDetectDuration;
    }

    public void setTraceDetectDuration(long traceDetectDuration) {
        this.traceDetectDuration = traceDetectDuration;
    }

    public int getTraceDetectSampleBatch() {
        return traceDetectSampleBatch;
    }

    public void setTraceDetectSampleBatch(int traceDetectSampleBatch) {
        this.traceDetectSampleBatch = traceDetectSampleBatch;
    }

    public int getTraceDetectSampleInterval() {
        return traceDetectSampleInterval;
    }

    public void setTraceDetectSampleInterval(int traceDetectSampleInterval) {
        this.traceDetectSampleInterval = traceDetectSampleInterval;
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }

    public String getAssertionsUserDef() {
        return assertionsUserDef;
    }

    public long getTimeoutDetectionDuration() {
        return timeoutDetectionDuration;
    }

    public int getTimeoutSampleBatch() {
        return timeoutSampleBatch;
    }

    public int getTimeoutSampleInterval() {
        return timeoutSampleInterval;
    }

    public long getRetryDetectionDuration() {
        return retryDetectionDuration;
    }

    public int getRetrySampleInterval() {
        return retrySampleInterval;
    }

    public int getRetrySampleBatch() {
        return retrySampleBatch;
    }

    public long getCircuitbreakerLocateDuration() {
        return circuitbreakerLocateDuration;
    }

    public int getCircuitbreakerLocateInterval() {
        return circuitbreakerLocateInterval;
    }

    public int getCircuitbreakerLocateSampleBatch() {
        return circuitbreakerLocateSampleBatch;
    }

    public int getCircuitbreakerQPSWindowSize() {
        return circuitbreakerQPSWindowSize;
    }

    public long getCircuitbreakerHalfStateDuration() {
        return circuitbreakerHalfStateDuration;
    }

    public int getCircuitbreakerInterval() {
        return circuitbreakerInterval;
    }

    public long getBulkheadDuration1() {
        return bulkheadDuration1;
    }

    public int getBulkheadWindowSize1() {
        return bulkheadWindowSize1;
    }

    public int getBulkheadInterval1() {
        return bulkheadInterval1;
    }

    public long getBulkheadDuration2() {
        return bulkheadDuration2;
    }

    public int getBulkheadWindowSize2() {
        return bulkheadWindowSize2;
    }

    public int getBulkheadInterval2() {
        return bulkheadInterval2;
    }

    public double getBulkheadThreshold() {
        return bulkheadThreshold;
    }
}
