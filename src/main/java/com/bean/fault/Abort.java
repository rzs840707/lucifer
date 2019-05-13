package com.iscas.bean.fault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Abort extends Fault {
    private int percent;
    private int code;

    public Abort(String tarService, String tarVersion, int percent, int code) {
        super(tarService, tarVersion);
        this.percent = percent;
        this.code = code;
    }

    @Override
    public JsonElement toJson() {
        JsonObject result = super.toJson().getAsJsonObject();
        String str = "{\"abort\":{\"percent\": " + this.percent + ",\"httpStatus\": " + this.code+ "}}";
        result.add("fault", new JsonParser().parse(str));
        return result;
    }
}
