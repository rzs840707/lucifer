package com.iscas.bean.result;

import javax.persistence.*;

@Entity
@Table(name = "Summary")
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int gid;
    private String id;
    private String startTime;
    private String endTime;
    private int injectTime;
    private int detectTime;
    private int timeoutTime;
    private int circuitBreakerTime;
    private int bulkheadTime;
    private int retryTime;

    public Summary() {
    }

    public Summary(String id, String startTime, String endTime,
                   int injectTime, int detectTime, int timeoutTime,
                   int circuitBreakerTime, int bulkheadTime, int retryTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.injectTime = injectTime;
        this.detectTime = detectTime;
        this.timeoutTime = timeoutTime;
        this.circuitBreakerTime = circuitBreakerTime;
        this.bulkheadTime = bulkheadTime;
        this.retryTime = retryTime;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getInjectTime() {
        return injectTime;
    }

    public void setInjectTime(int injectTime) {
        this.injectTime = injectTime;
    }

    public int getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(int detectTime) {
        this.detectTime = detectTime;
    }

    public int getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(int timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public int getCircuitBreakerTime() {
        return circuitBreakerTime;
    }

    public void setCircuitBreakerTime(int circuitBreakerTime) {
        this.circuitBreakerTime = circuitBreakerTime;
    }

    public int getBulkheadTime() {
        return bulkheadTime;
    }

    public void setBulkheadTime(int bulkheadTime) {
        this.bulkheadTime = bulkheadTime;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }
}
