package com.iscas.entity;

import java.util.ArrayList;
import java.util.List;

public class Result {
    private boolean success;
    private List<Object> data;
    private String message;

    public Result(boolean success, List<Object> data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        if (this.data == null)
            this.data = new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
