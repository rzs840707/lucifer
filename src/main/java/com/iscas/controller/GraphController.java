package com.iscas.controller;


import com.iscas.entity.Graph;
import com.iscas.entity.Result;
import com.iscas.service.Telemetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

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

    @RequestMapping("/prom")
    public Result getPromData(@RequestParam String metric, @RequestParam String srcSvc, @RequestParam String tarSvc) {
        String query;
        switch (metric) {
            case "successRate":
                query = "sum(rate(istio_requests_total{reporter=\"source\", " +
                        "response_code!~\"5.*\",source_app=\"" + srcSvc + "\",destination_service_name=\"" + tarSvc + "\"}[1m])) by (le,response_code)" +
                        " / sum(rate(istio_requests_total{reporter=\"source\"," +
                        "source_app=\"" + srcSvc + "\",destination_service_name=\"" + tarSvc + "\"}[1m])) by (le,response_code)";
                break;
            case "failRate":
                query = "sum(rate(istio_requests_total{reporter=\"source\", " +
                        "response_code!~\"2.*\",source_app=\"" + srcSvc + "\",destination_service_name=\"" + tarSvc + "\"}[1m])) by (le,response_code)" +
                        " / sum(rate(istio_requests_total{reporter=\"source\"," +
                        "source_app=\"" + srcSvc + "\",destination_service_name=\"" + tarSvc + "\"}[1m])) by (le,response_code)";
                break;
            case "rate":
                query="sum(round(irate(istio_requests_total{reporter=\"source\",source_app=\""+srcSvc
                        +"\",destination_service_name=\""+tarSvc+"\"}[1m]) , 0.001)) by (le,response_code)";
                break;
            case "responseTime":
                query = "histogram_quantile(0.99, sum(rate(istio_request_duration_seconds_bucket{reporter=\"source\"," +
                        "source_app=\"" + srcSvc + "\",destination_service_name=\"" + tarSvc + "\"}[1m])) by (le, response_code))";
                break;
            default:
                query = null;
        }
        if (query == null)
            return new Result(false, null, "属性值存在空值");
        return new Result(true, Arrays.asList(telemetry.queryRange(query)), "");
    }
}
