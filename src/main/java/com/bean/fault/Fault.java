package com.iscas.bean.fault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Fault {
    // 路由部分
    private String tarService;
    private String tarVersion;

    public Fault(String tarService, String tarVersion) {
        this.tarService = tarService;
        this.tarVersion = tarVersion;
    }

    public JsonElement toJson() {
        String str = "{\"destination\":{\"host\":\"" + this.tarService + "\",\"subset\":\"" + this.tarVersion + "\"}}";
        JsonArray tmp = new JsonArray();
        tmp.add(new JsonParser().parse(str));
        JsonObject result = new JsonObject();
        result.add("route", tmp);
        return result;
    }

    public String getTarService() {
        return tarService;
    }
}
