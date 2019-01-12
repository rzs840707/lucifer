package com.iscas.bean.fault;

public class Delay extends Fault {
    private int percent;
    private int duration;

    public Delay(String tarService, String tarVersion, String srcService, int percent, int duration) {
        super(tarService, tarVersion, srcService);
        this.percent = percent;
        this.duration = duration;
    }
}
