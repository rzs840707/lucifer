package com.iscas.service;

import com.google.gson.*;
import com.iscas.bean.fault.Fault;
import com.iscas.entity.Route;
import com.iscas.util.Cmd;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
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

    public void injectdelay(String src, String tar, String tarVersion, int percent, long duration) {
        JsonObject root = new JsonObject();

        //添加总体的参数
        root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
        root.add("kind", new JsonPrimitive("VirtualService"));
        root.add("metadata", new JsonParser().parse("{\"name\":\"" + tar + "\"}"));
        JsonObject spec = new JsonObject();
        spec.add("hosts", new JsonParser().parse("[\"" + tar + "\"]"));
        JsonArray http = new JsonArray();
        spec.add("http", http);
        root.add("spec", spec);

        JsonObject result = new JsonObject();
        String str = "{\"sourceLabels\":{\"app\":\"" + src + "\"}}";
        JsonArray tmp = new JsonArray();
        tmp.add(new JsonParser().parse(str));
        result.add("match", tmp);
        str = "{\"destination\":{\"host\":\"" + tar + "\",\"subset\":\"" + tarVersion + "\"}}";
        tmp = new JsonArray();
        tmp.add(new JsonParser().parse(str));
        result.add("route", tmp);
        str = "{\"delay\":{\"percent\": " + percent + ",\"fixedDelay\": \"" + duration + "s\"}}";
        result.add("fault", new JsonParser().parse(str));
        http.add(result);

        //查询出规则列表
        JsonArray rules = queryRules(tar);
        // 添加原有的故障信息
        if (rules != null && rules.size() > 0)
            http.addAll(rules);

        // 注入
        Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
    }

    public void injectAbort(String src, String tar, String tarVersion, int percent, int httpcode) {
        JsonObject root = new JsonObject();

        //添加总体的参数
        root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
        root.add("kind", new JsonPrimitive("VirtualService"));
        root.add("metadata", new JsonParser().parse("{\"name\":\"" + tar + "\"}"));
        JsonObject spec = new JsonObject();
        spec.add("hosts", new JsonParser().parse("[\"" + tar + "\"]"));
        JsonArray http = new JsonArray();
        spec.add("http", http);
        root.add("spec", spec);

        JsonObject result = new JsonObject();
        String str = "{\"sourceLabels\":{\"app\":\"" + src + "\"}}";
        JsonArray tmp = new JsonArray();
        tmp.add(new JsonParser().parse(str));
        result.add("match", tmp);
        str = "{\"destination\":{\"host\":\"" + tar + "\",\"subset\":\"" + tarVersion + "\"}}";
        tmp = new JsonArray();
        tmp.add(new JsonParser().parse(str));
        result.add("route", tmp);
        str = "{\"abort\":{\"percent\": " + percent + ",\"httpStatus\": " + httpcode + "}}";
        result.add("fault", new JsonParser().parse(str));
        http.add(result);

        //查询出规则列表
        JsonArray rules = queryRules(tar);
        // 添加原有的故障信息
        if (rules != null && rules.size() > 0)
            http.addAll(rules);

        // 注入
        Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
    }

    public void delete(Fault fault) {
        Cmd.exec("kubectl delete virtualservice " + fault.getTarService());
    }

    public void deleteByName(String name) {
        Cmd.exec("kubectl delete virtualservice " + name);
    }

    private JsonArray queryRules(String name) {
        String str = Cmd.execForStd("kubectl get virtualservice " + name + " -o json");
        if (str.isEmpty())
            return null;
        JsonObject root = new JsonParser().parse(str).getAsJsonObject();
        return root.getAsJsonObject("spec").getAsJsonArray("http");
    }

    public void deleteFault(String name, int index) {
        JsonObject root = new JsonObject();

        // 查询已有规则
        JsonArray http = queryRules(name);
        if (http == null || http.size() == 0)
            return;

        http.remove(index);

        // 如果规则列表空了，就删除配置
        if (http.size() == 0)
            this.deleteByName(name);
        else {
            //重新配置
            root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
            root.add("kind", new JsonPrimitive("VirtualService"));
            root.add("metadata", new JsonParser().parse("{\"name\":\"" + name + "\"}"));
            JsonObject spec = new JsonObject();
            spec.add("hosts", new JsonParser().parse("[\"" + name + "\"]"));
            spec.add("http", http);
            root.add("spec", spec);
            Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
        }
    }

    public List<com.iscas.entity.Fault> queryAllFaults() {
        List<com.iscas.entity.Fault> r = new ArrayList<>();
        String tmp = Cmd.execForStdWithPipe("kubectl  get virtualservices | grep -v NAME | awk '{print $1 \";\"}'");
        if (!tmp.isEmpty()) {
            String[] tmpArr = tmp.split(";");
            for (int i = 0; i < tmpArr.length; ++i) {
                String name = tmpArr[i];
                tmp = Cmd.execForStd("kubectl get virtualservice " + name + " -o json");
                JsonObject root = new JsonParser().parse(tmp).getAsJsonObject();
                JsonArray http = root.getAsJsonObject("spec").getAsJsonArray("http");
                for (int j = 0; j < http.size(); ++j) {
                    JsonObject rule = http.get(j).getAsJsonObject();
                    com.iscas.entity.Fault fault = new com.iscas.entity.Fault();
                    // 反序列化fault标签
                    if (rule.get("fault") == null)
                        continue;
                    if (rule.getAsJsonObject("fault").get("delay") != null) {
                        JsonObject f = rule.getAsJsonObject("fault").getAsJsonObject("delay");
                        fault.setType("delay");
                        fault.setParam(f.get("fixedDelay").getAsString());
                        fault.setPercent(f.get("percent").getAsInt());
                    } else if (rule.getAsJsonObject("fault").get("abort") != null) {
                        JsonObject f = rule.getAsJsonObject("fault").getAsJsonObject("abort");
                        fault.setType("abort");
                        fault.setParam(String.valueOf(f.get("httpStatus").getAsInt()));
                        fault.setPercent(f.get("percent").getAsInt());
                    }
                    // 反序列化match
                    if (rule.get("match") != null) {
                        JsonArray match = rule.getAsJsonArray("match");
                        if (match.get(0).getAsJsonObject().getAsJsonObject("sourceLabels") != null)
                            fault.setSrc(match.get(0).getAsJsonObject().getAsJsonObject("sourceLabels").get("app").getAsString());
                    }
                    //反序列化route
                    JsonObject destination = rule.getAsJsonArray("route").get(0)
                            .getAsJsonObject().getAsJsonObject("destination");
                    fault.setTar(destination.get("host").getAsString());
                    fault.setTarVersion(destination.get("subset").getAsString());
                    //反序列化name
                    fault.setName(root.getAsJsonObject("metadata").get("name").getAsString());
                    fault.setIndex(j);
                    r.add(fault);
                }
            }
        }
        return r;
    }

    public void injectRoute(String tar, String tarVersion, int percent) {
        //添加总体的参数
        JsonObject root = new JsonObject();
        root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
        root.add("kind", new JsonPrimitive("VirtualService"));
        root.add("metadata", new JsonParser().parse("{\"name\":\"" + tar + "\"}"));
        JsonObject spec = new JsonObject();
        spec.add("hosts", new JsonParser().parse("[\"" + tar + "\"]"));
        JsonArray http = new JsonArray();
        spec.add("http", http);
        root.add("spec", spec);

        // 查询已有虚服务
        JsonArray rules = queryRules(tar);
        if (rules == null) // 不存在已有虚服务
        {
            String str = "{\"destination\":{\"host\":\"" + tar + "\",\"subset\":\"" + tarVersion + "\"}}";
            JsonObject route = new JsonParser().parse(str).getAsJsonObject();
            route.add("weight", new JsonPrimitive(percent));
            JsonArray routes = new JsonArray();
            routes.add(route);
            JsonObject rule = new JsonObject();
            rule.add("route", routes);
            http.add(rule);
        } else {
            for (int i = 0; i < rules.size() - 1; ++i) {
                http.add(rules.get(i));
            }
            JsonObject lastRule = rules.get(rules.size() - 1).getAsJsonObject();
            if (lastRule.get("fault") == null && lastRule.get("match") == null) {
                String str = "{\"destination\":{\"host\":\"" + tar + "\",\"subset\":\"" + tarVersion + "\"}}";
                JsonObject route = new JsonParser().parse(str).getAsJsonObject();
                route.add("weight", new JsonPrimitive(percent));
                lastRule.getAsJsonArray("route").add(route);
                http.add(lastRule);
            } else {
                http.add(lastRule);
                String str = "{\"destination\":{\"host\":\"" + tar + "\",\"subset\":\"" + tarVersion + "\"}}";
                JsonObject route = new JsonParser().parse(str).getAsJsonObject();
                route.add("weight", new JsonPrimitive(percent));
                JsonArray routes = new JsonArray();
                routes.add(route);
                JsonObject rule = new JsonObject();
                rule.add("route", routes);
                http.add(rule);
            }
        }

        // 注入
        Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
    }

    public void deleteRoute(String name, int index) {
        //添加总体的参数
        JsonObject root = new JsonObject();
        root.add("apiVersion", new JsonPrimitive("networking.istio.io/v1alpha3"));
        root.add("kind", new JsonPrimitive("VirtualService"));
        root.add("metadata", new JsonParser().parse("{\"name\":\"" + name + "\"}"));
        JsonObject spec = new JsonObject();
        spec.add("hosts", new JsonParser().parse("[\"" + name + "\"]"));
        JsonArray http = new JsonArray();
        spec.add("http", http);
        root.add("spec", spec);

        JsonArray rules = queryRules(name);
        for (int i = 0; i < rules.size() - 1; ++i) {
            http.add(rules.get(i));
        }
        JsonObject lastRule = rules.get(rules.size() - 1).getAsJsonObject();
        if (lastRule.get("fault") == null && lastRule.get("match") == null) {
            JsonArray routes = lastRule.getAsJsonArray("route");
            routes.remove(index);
            if (routes.size() > 0) {
                lastRule.add("route", routes);
                http.add(lastRule);
            }
        } else {
            http.add(lastRule);
        }
        // 注入
        if (http.size() == 0)
            deleteByName(name);
        else
            Cmd.execWithPipe("echo '" + root.toString() + "' | kubectl apply -f -");
    }

    public List<Route> queryAllRoutes() {
        List<Route> r = new ArrayList<>();
        String tmp = Cmd.execForStdWithPipe("kubectl  get virtualservices | grep -v NAME | awk '{print $1 \";\"}'");
        if (!tmp.isEmpty()) {
            String[] tmpArr = tmp.split(";");
            for (int i = 0; i < tmpArr.length; ++i) {
                String name = tmpArr[i];
                tmp = Cmd.execForStd("kubectl get virtualservice " + name + " -o json");
                JsonObject root = new JsonParser().parse(tmp).getAsJsonObject();
                JsonArray http = root.getAsJsonObject("spec").getAsJsonArray("http");
                if (http.size() > 0) {
                    JsonObject lastRule = http.get(http.size() - 1).getAsJsonObject();
                    if (lastRule.get("fault") == null && lastRule.get("match") == null) {
                        JsonArray routes = lastRule.getAsJsonArray("route");
                        for (int j = 0; j < routes.size(); ++j) {
                            JsonObject route = routes.get(j).getAsJsonObject();
                            Route tmpRoute = new Route();
                            tmpRoute.setIndex(j);
                            tmpRoute.setTar(route.getAsJsonObject("destination").get("host").getAsString());
                            tmpRoute.setTarVersion(route.getAsJsonObject("destination").get("subset").getAsString());
                            if (route.get("weight") == null)
                                tmpRoute.setPercent(100);
                            else
                                tmpRoute.setPercent(route.get("weight").getAsInt());
                            tmpRoute.setName(name);
                            r.add(tmpRoute);
                        }
                    }
                }
            }
        }
        return r;
    }
}
