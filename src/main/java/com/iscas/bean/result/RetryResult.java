package com.iscas.bean.result;

import javax.persistence.*;

@Entity
@Table(name = "RetryResult")
public class RetryResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gid;
    private String id;
    private int injectTime;
    private String url;
    private String srcName;
    private String targetName;
    private int threshold;
    private String message;
    private String timestamp;

    public RetryResult() {
    }

    public RetryResult(String id, int injectTime, String url, String srcName,
                       String targetName, int threshold, String message, String timestamp) {
        this.id = id;
        this.injectTime = injectTime;
        this.url = url;
        this.srcName = srcName;
        this.targetName = targetName;
        this.threshold = threshold;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public int getInjectTime() {
        return injectTime;
    }

    public String getUrl() {
        return url;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }
}
