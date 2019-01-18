package com.iscas.controller;


import com.iscas.entity.Graph;
import com.iscas.service.Telemetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private Telemetry telemetry;

    @Autowired
    public GraphController(Telemetry telemetry) {
        this.telemetry = telemetry;
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
