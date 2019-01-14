package com.iscas.bean;

public class Assertion {
    private String name;
    private String url;
    private String message;

    public Assertion(String name,String url, String message) {
        this.name = name;
        this.url = url;
        this.message = message;
    }
}
