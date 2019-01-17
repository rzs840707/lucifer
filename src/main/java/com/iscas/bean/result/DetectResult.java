package com.iscas.bean.result;

import com.iscas.bean.Assertion;

import java.util.List;

public class DetectResult {
    private String id;
    private int injectionTime;
    private String message;
    private String timestamp;

    public DetectResult(String id, int injectionTime, String message, String timestamp) {
        this.id = id;
        this.injectionTime = injectionTime;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInjectionTime() {
        return injectionTime;
    }

    public void setInjectionTime(int injectionTime) {
        this.injectionTime = injectionTime;
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
}
