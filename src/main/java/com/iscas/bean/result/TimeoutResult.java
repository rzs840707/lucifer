package com.iscas.bean.result;

import javax.persistence.*;

@Entity
@Table(name = "TimeoutResult")
public class TimeoutResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int gid;
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double threshold;
    private String message;
    private String timestamp;

    public TimeoutResult() {
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInjectTime() {
        return injectTime;
    }

    public void setInjectTime(int injectTime) {
        this.injectTime = injectTime;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }
}
