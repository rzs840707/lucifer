package com.iscas.bean.fault;

public class Abort extends Fault {
    private int percent;
    private int code;

    public Abort(String tarService, String tarVersion, String srcService, int percent, int code) {
        super(tarService, tarVersion, srcService);
        this.percent = percent;
        this.code = code;
    }

}
