package com.iscas.entity;

public class Edge {
    private String srcName;
    private String tarName;
    private String value;

    public Edge(String srcName, String tarName, String value) {
        this.srcName = srcName;
        this.tarName = tarName;
        this.value = value;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getTarName() {
        return tarName;
    }

    public void setTarName(String tarName) {
        this.tarName = tarName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return (srcName + tarName).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge))
            return false;
        return this.hashCode() == obj.hashCode();
    }
}
