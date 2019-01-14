package com.iscas.service;

import com.google.gson.*;
import com.iscas.bean.fault.Fault;
import com.iscas.util.Cmd;

public class Injector {
    public void inject(Fault fault) {
        JsonObject root = new JsonObject();

        //添加总体的参数
        root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
        root.add("kind", new JsonPrimitive("VirtualService"));
        root.add("metadata", new JsonParser().parse("{\"name\":\"" + fault.getTarService() + "\"}"));
        JsonObject spec = new JsonObject();
        spec.add("hosts", new JsonParser().parse("[\"" + fault.getTarService() + "\"]"));
        JsonArray http = new JsonArray();
        spec.add("http", http);

        // 添加新的故障信息
        http.add(fault.toJson());

        //查询出规则列表
        JsonArray rules = queryRules(fault.getTarService());
        // 添加原有的故障信息
        if (rules != null && rules.size() > 0)
            http.addAll(rules);

        // 注入
        Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
    }

    public void delete(Fault fault) {
        JsonObject root = new JsonObject();

        // 查询已有规则
        JsonArray rules = queryRules(fault.getTarService());
        if (rules == null || rules.size() == 0)
            return;

        // 删除已有故障
        JsonElement itemToDelete = fault.toJson();
        for (int i = 0; i < rules.size(); ++i) {
            if (rules.get(i).equals(itemToDelete)) {
                rules.remove(i);
                break;
            }
        }

        // 如果规则列表空了，就删除配置
        if (rules.size() == 0)
            this.deleteVirtualservice(fault.getTarService());
        else {
            //重新配置
            root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
            root.add("kind", new JsonPrimitive("VirtualService"));
            root.add("metadata", new JsonParser().parse("{\"name\":\"" + fault.getTarService() + "\"}"));
            JsonObject spec = new JsonObject();
            spec.add("hosts", new JsonParser().parse("[\"" + fault.getTarService() + "\"]"));
            spec.add("http", rules);
            Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
        }

    }

    private JsonArray queryRules(String name) {
        String str = Cmd.execForStd("kubectl get virtualservice " + name + " -o json");
        if (str.startsWith("Error from server (NotFound):"))
            return null;
        JsonObject root = new JsonParser().parse(str).getAsJsonObject();
        return root.getAsJsonObject("spec").getAsJsonArray("http");
    }

    public void deleteVirtualservice(String name) {
        Cmd.exec("kubectl delete virtualservice " + name);
    }
}
