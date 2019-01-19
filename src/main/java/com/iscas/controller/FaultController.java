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
@RequestMapping("/fault")
public class FaultController {

    private Injector injector;
    private Telemetry telemetry;

    @Autowired
    public FaultController(Injector injector, Telemetry telemetry) {
        this.injector = injector;
        this.telemetry = telemetry;
    }

    @RequestMapping("/faults")
    public Result getAllFaults() {
        return new Result(true, new ArrayList<>(injector.queryAllFaults()), "");
    }

    @RequestMapping(value = "/delay", method = RequestMethod.POST)
    public Result addDelay(@RequestBody Map<String, Object> reqMap) {
        String src = reqMap.get("src").toString();
        String tar = reqMap.get("tar").toString();
        String tarVersion = reqMap.get("tarVersion").toString();
        int percent = Integer.valueOf(reqMap.get("percent").toString());
        long duration = Long.valueOf(reqMap.get("percent").toString());
        injector.injectdelay(src, tar, tarVersion, percent, duration);
        return new Result(true, null, null);
    }

    @RequestMapping(value = "/abort", method = RequestMethod.POST)
    public Result addAbort(@RequestBody Map<String, Object> reqMap) {
        String src = reqMap.get("src").toString();
        String tar = reqMap.get("tar").toString();
        String tarVersion = reqMap.get("tarVersion").toString();
        int percent = Integer.valueOf(reqMap.get("percent").toString());
        int httpcode = Integer.valueOf(reqMap.get("httpcode").toString());
        injector.injectAbort(src, tar, tarVersion, percent, httpcode);
        return new Result(true, null, null);
    }

    @RequestMapping(value = "/faultDelete")
    public Result deleteFault(@RequestParam String name, @RequestParam int index) {
        injector.deleteFault(name, index);
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
