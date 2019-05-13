package com.iscas.controller;

import com.iscas.entity.Graph;
import com.iscas.entity.Result;
import com.iscas.service.Injector;
import com.iscas.service.Telemetry;
import com.iscas.util.Cmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/route")
public class RouteController {
    private Injector injector;
    private Telemetry telemetry;

    @Autowired
    public RouteController(Injector injector, Telemetry telemetry) {
        this.injector = injector;
        this.telemetry = telemetry;
    }

    @RequestMapping("/routes")
    public Result getAllRoutes() {
        return new Result(true, new ArrayList<>(injector.queryAllRoutes()), "");
    }

    @RequestMapping(value = "/route", method = RequestMethod.POST)
    public Result addRoute(@RequestBody Map<String, Object> reqMap) {
        String tar = reqMap.get("tar").toString();
        String tarVersion = reqMap.get("tarVersion").toString();
        int percent = Integer.valueOf(reqMap.get("percent").toString());
        injector.injectRoute(tar,tarVersion,percent);
        return new Result(true, null, null);
    }

    @RequestMapping(value = "/RouteDelete")
    public Result deleteRoute(@RequestParam String name, @RequestParam int index) {
        injector.deleteRoute(name, index);
        return new Result(true, null, null);
    }

    @RequestMapping("/export/{name}")
    public void export(HttpServletResponse response, @PathVariable String name) {
        response.setContentType("application/force-download");// 设置强制下载不打开
        response.addHeader("Content-Disposition", "attachment;fileName=" + name + ".yaml");// 设置文件名
        try {
            String str = Cmd.execForStd2("kubectl get virtualservice " + name + " -o yaml");
            response.getOutputStream().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportAll")
    public void export(HttpServletResponse response) {
        response.setContentType("application/force-download");// 设置强制下载不打开
        response.addHeader("Content-Disposition", "attachment;fileName=" + "all.yaml");// 设置文件名
        try {
            String str = Cmd.execForStd2("kubectl get virtualservices -o yaml");
            response.getOutputStream().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/responseTimeGraph")
    public Graph getResponseTimeGraph() {
        return telemetry.queryCurrentResponseTime();
    }

    @RequestMapping("/throughputGraph")
    public Graph getThroughputGraph() {
        return telemetry.queryCurrentThroughoutGraph();
    }
}
