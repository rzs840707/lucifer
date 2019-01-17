package com.iscas.bean;

public class Assertion {
    private String name;
    private String url;
    private String message;

    public Assertion(String name, String url, String message) {
        this.name = name;
        this.url = url;
        this.message = message;
    }

    @Override
    public String toString() {
        return "名称:" + name + ",地址:" + url + ",信息:" + message;
    }
}
