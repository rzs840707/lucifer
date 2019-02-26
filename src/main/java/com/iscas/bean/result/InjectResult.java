package com.iscas.bean.result;

import javax.persistence.*;

@Entity
@Table(name = "InjectResult")
public class InjectResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gid;
    private String id;
    private int injectId;
    private String ip;
    private String timestamp;
    private boolean normal;
    private String traceID;

    public InjectResult() {
    }

    public InjectResult(String id, int injectId, String ip, String timestamp, boolean normal, String traceID) {
        this.id = id;
        this.injectId = injectId;
        this.ip = ip;
        this.timestamp = timestamp;
        this.normal = normal;
        this.traceID = traceID;
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

    public int getInjectId() {
        return injectId;
    }

    public void setInjectId(int injectId) {
        this.injectId = injectId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }
}
