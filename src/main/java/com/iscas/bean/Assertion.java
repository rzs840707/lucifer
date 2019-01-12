package com.iscas.bean;

public class Assertion {
    private String name;
    private int url;
    private String message;

    public Assertion(String name,int url, String message) {
        this.name = name;
        this.url = url;
        this.message = message;
    }
}
