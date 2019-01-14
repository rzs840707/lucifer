package com.iscas.bean;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    // service scope
    String[] services;

    // trace detection
    private long traceDetectDuration; // seconds
    private int traceDetectSampleBatch;
    private int traceDetectSampleInterval; // seconds

    // assertions
    private List<String> assertionsUserDef; // 用户响应是否正常的判断依据(用户定义的python文件)

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

    public Configuration() {
        // 设置trace探测阶段
        this.traceDetectDuration = 60;
        this.traceDetectSampleBatch = 20;
        this.traceDetectSampleInterval = 3;

        // 设置断言验证
        this.assertionsUserDef = new ArrayList<>();
        this.assertionsUserDef.add("assertions/productpage.py");

        // 设置超时
        this.timeoutDetectionDuration = 5 * 60;
        this.timeoutSampleBatch = 20;
        this.timeoutSampleInterval = 3;

        // 设置重试
        this.retryDetectionDuration = 5 * 60;
        this.retrySampleBatch = 20;
        this.retrySampleInterval = 3;

        //设置熔断
        this.circuitbreakerLocateDuration = 5 * 60;
        this.circuitbreakerLocateSampleBatch = 20;
        this.circuitbreakerLocateInterval = 3;
        this.circuitbreakerHalfStateDuration = 3;
        this.circuitbreakerQPSWindowSize = 3;
        this.circuitbreakerInterval = 1;

        //设置船舱
        this.bulkheadDuration1 = 3 * 60;
        this.bulkheadWindowSize1 = 3;
        this.bulkheadInterval1 = 1;
        this.bulkheadDuration2 = 3 * 60;
        this.bulkheadWindowSize2 = 3;
        this.bulkheadInterval2 = 1;
        this.bulkheadThreshold = 0.4;
    }

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

    public List<String> getAssertionsUserDef() {
        return assertionsUserDef;
    }
}
