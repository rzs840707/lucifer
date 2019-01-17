package com.iscas.bean;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class Trace {
    private int count;
    private String[] urls;
    private String[] services;

    public Trace(String[] urls, String[] services) {
        this.urls = urls;
        Arrays.sort(services);
        this.services = services;
        this.count = 1;
    }

    @Override
    public int hashCode() {
        String token = StringUtils.join(this.services, ';');
        return token.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Trace))
            return false;
        return this.hashCode() == obj.hashCode();
    }

    public void increaseCount() {
        this.count += 1;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        Arrays.sort(services);
        this.services = services;
    }

    public int getCount() {
        return count;
    }
}
