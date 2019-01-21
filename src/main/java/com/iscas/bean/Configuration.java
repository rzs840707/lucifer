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
        this.traceDetectDuration = 1 * 30;
        this.traceDetectSampleBatch = 20;
        this.traceDetectSampleInterval = 30;

        // 设置断言验证
        this.assertionsUserDef = new ArrayList<>();
        this.assertionsUserDef.add("/home/ccx/dev/projects/java/lucifer/src/main/resources/assertions/productpage.py");

        // 设置超时
        this.timeoutDetectionDuration = 2 * 30;
        this.timeoutSampleBatch = 20;
        this.timeoutSampleInterval = 30;

        // 设置重试
        this.retryDetectionDuration = 2 * 30;
        this.retrySampleBatch = 20;
        this.retrySampleInterval = 30;

        //设置熔断
        this.circuitbreakerLocateDuration = 2 * 30;
        this.circuitbreakerLocateSampleBatch = 20;
        this.circuitbreakerLocateInterval = 30;
        this.circuitbreakerHalfStateDuration = 2 * 30;
        this.circuitbreakerQPSWindowSize = 10;
        this.circuitbreakerInterval = 10;

        //设置船舱
        this.bulkheadDuration1 = 2 * 30;
        this.bulkheadWindowSize1 = 10;
        this.bulkheadInterval1 = 10;
        this.bulkheadDuration2 = 2 * 30;
        this.bulkheadWindowSize2 = 10;
        this.bulkheadInterval2 = 10;
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

    public void setAssertionsUserDef(List<String> assertionsUserDef) {
        this.assertionsUserDef = assertionsUserDef;
    }

    public void setTimeoutDetectionDuration(long timeoutDetectionDuration) {
        this.timeoutDetectionDuration = timeoutDetectionDuration;
    }

    public void setTimeoutSampleInterval(int timeoutSampleInterval) {
        this.timeoutSampleInterval = timeoutSampleInterval;
    }

    public void setTimeoutSampleBatch(int timeoutSampleBatch) {
        this.timeoutSampleBatch = timeoutSampleBatch;
    }

    public void setRetryDetectionDuration(long retryDetectionDuration) {
        this.retryDetectionDuration = retryDetectionDuration;
    }

    public void setRetrySampleInterval(int retrySampleInterval) {
        this.retrySampleInterval = retrySampleInterval;
    }

    public void setRetrySampleBatch(int retrySampleBatch) {
        this.retrySampleBatch = retrySampleBatch;
    }

    public void setCircuitbreakerLocateDuration(long circuitbreakerLocateDuration) {
        this.circuitbreakerLocateDuration = circuitbreakerLocateDuration;
    }

    public void setCircuitbreakerLocateInterval(int circuitbreakerLocateInterval) {
        this.circuitbreakerLocateInterval = circuitbreakerLocateInterval;
    }

    public void setCircuitbreakerLocateSampleBatch(int circuitbreakerLocateSampleBatch) {
        this.circuitbreakerLocateSampleBatch = circuitbreakerLocateSampleBatch;
    }

    public void setCircuitbreakerQPSWindowSize(int circuitbreakerQPSWindowSize) {
        this.circuitbreakerQPSWindowSize = circuitbreakerQPSWindowSize;
    }

    public void setCircuitbreakerHalfStateDuration(long circuitbreakerHalfStateDuration) {
        this.circuitbreakerHalfStateDuration = circuitbreakerHalfStateDuration;
    }

    public void setCircuitbreakerInterval(int circuitbreakerInterval) {
        this.circuitbreakerInterval = circuitbreakerInterval;
    }

    public void setBulkheadDuration1(long bulkheadDuration1) {
        this.bulkheadDuration1 = bulkheadDuration1;
    }

    public void setBulkheadWindowSize1(int bulkheadWindowSize1) {
        this.bulkheadWindowSize1 = bulkheadWindowSize1;
    }

    public void setBulkheadInterval1(int bulkheadInterval1) {
        this.bulkheadInterval1 = bulkheadInterval1;
    }

    public void setBulkheadDuration2(long bulkheadDuration2) {
        this.bulkheadDuration2 = bulkheadDuration2;
    }

    public void setBulkheadWindowSize2(int bulkheadWindowSize2) {
        this.bulkheadWindowSize2 = bulkheadWindowSize2;
    }

    public void setBulkheadInterval2(int bulkheadInterval2) {
        this.bulkheadInterval2 = bulkheadInterval2;
    }

    public void setBulkheadThreshold(double bulkheadThreshold) {
        this.bulkheadThreshold = bulkheadThreshold;
    }
}
