package com.iscas.entity;

public class Fault {
    private String name;
    private String type;
    private String src;
    private String tar;
    private String tarVersion;
    private int percent;
    private String param;
    private int index;

    public Fault() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTar() {
        return tar;
    }

    public void setTar(String tar) {
        this.tar = tar;
    }

    public String getTarVersion() {
        return tarVersion;
    }

    public void setTarVersion(String tarVersion) {
        this.tarVersion = tarVersion;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
