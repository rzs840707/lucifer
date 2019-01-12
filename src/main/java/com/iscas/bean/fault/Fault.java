package com.iscas.bean.fault;

public class Fault {
    private String tarService;
    private String tarVersion;
    private String srcService;

    public Fault(String tarService, String tarVersion, String srcService) {
        this.tarService = tarService;
        this.tarVersion = tarVersion;
        this.srcService = srcService;
    }
}
