package com.iscas.entity;

public class Route {
    private String name;
    private String tar;
    private String tarVersion;
    private int percent;
    private int index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
