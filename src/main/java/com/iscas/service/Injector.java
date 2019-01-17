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
        root.add("spec", spec);

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
        Cmd.exec("kubectl delete virtualservice " + fault.getTarService());
    }

    private JsonArray queryRules(String name) {
        String str = Cmd.execForStd("kubectl get virtualservice " + name + " -o json");
        if (str.isEmpty())
            return null;
        JsonObject root = new JsonParser().parse(str).getAsJsonObject();
        return root.getAsJsonObject("spec").getAsJsonArray("http");
    }

//    public void deleteByFault(Fault fault) {
//        JsonObject root = new JsonObject();
//
//        // 查询已有规则
//        JsonArray http = queryRules(fault.getTarService());
//        if (http == null || http.size() == 0)
//            return;
//
//        // 删除已有故障
//        JsonElement itemToDelete = fault.toJson();
//        for (int i = 0; i < http.size(); ++i) {
//            if (http.get(i).equals(itemToDelete)) {
//                http.remove(i);
//                break;
//            }
//        }
//
//        // 如果规则列表空了，就删除配置
//        if (http.size() == 0)
//            this.deleteVirtualservice(fault.getTarService());
//        else {
//            //重新配置
//            root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
//            root.add("kind", new JsonPrimitive("VirtualService"));
//            root.add("metadata", new JsonParser().parse("{\"name\":\"" + fault.getTarService() + "\"}"));
//            JsonObject spec = new JsonObject();
//            spec.add("hosts", new JsonParser().parse("[\"" + fault.getTarService() + "\"]"));
//            spec.add("http", http);
//            root.add("spec", spec);
//            Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
//        }
//    }
}
