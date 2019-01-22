package com.iscas.bean.result;

import javax.persistence.*;

@Entity
@Table(name = "BulkHeadResult")
public class BulkHeadResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gid;
    private String id;
    private int injectTime;
    private String srcName;
    private String targetName;
    private double normalthroughout;
    private double worstThroughout;
    private String message;
    private String timestamp;

    public BulkHeadResult() {
    }

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

    public double getNormalthroughout() {
        return normalthroughout;
    }

    public double getWorstThroughout() {
        return worstThroughout;
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
