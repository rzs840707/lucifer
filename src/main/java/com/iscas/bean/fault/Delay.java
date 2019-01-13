package com.iscas.bean.fault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Delay extends Fault {
    private int percent;
    private int duration;

    public Delay(String tarService, String tarVersion, int percent, int duration) {
        super(tarService, tarVersion);
        this.percent = percent;
        this.duration = duration;
    }

    @Override
    public JsonElement toJson() {
        JsonObject result = super.toJson().getAsJsonObject();
        String str = "{\"delay\":{\"percent\": " + this.percent + ",\"fixedDelay\": \"" + this.duration + "s\"}}";
        result.add("fault", new JsonParser().parse(str));
        return result;
    }
}
