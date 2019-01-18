package com.iscas.controller;

import com.iscas.bean.Trace;
import com.iscas.bean.result.BulkHeadResult;
import com.iscas.bean.result.CircuitBreakResult;
import com.iscas.bean.result.RetryResult;
import com.iscas.bean.result.TimeoutResult;
import com.iscas.entity.Graph;
import com.iscas.entity.Result;
import com.iscas.service.Telemetry;
import com.iscas.strategy.Heuristic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/exp")
public class ExperimentController {

    private Thread t;

//    @Autowired
//    private SimpMessagingTemplate test;

    @Autowired
    private Heuristic heuristic;

    @Autowired
    private Telemetry telemetry;

    @RequestMapping("/start")
    public Result start() {
        if (t != null && t.isAlive()) {
            return new Result(false, null, "正在运行探测过程,无法开启新的探测");
        }
        t = new Thread(() -> this.heuristic.run());
        t.start();
        return new Result(true, null, "成功开启探测");
    }

    @RequestMapping("/stop")
    public Result stop() throws InterruptedException {
        if (t != null && t.isAlive()) {
            heuristic.setStop(true);
            while (t.isAlive()) {
                Thread.sleep(2 * 1000);
            }
            return new Result(true, null, "已停止当前探测过程");
        } else
            return new Result(true, null, "不存在正在运行的探测过程");
    }

    @RequestMapping("/retryResult")
    public Result getRetryResult() {
        List<RetryResult> r = this.heuristic.getRrs();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/timeoutResult")
    public Result getTimeoutResult() {
//        // 测试
//        List<TimeoutResult> tmp = new ArrayList<>();
//        Random r = new Random();
//        tmp.add(new TimeoutResult(r.nextInt(20) + "", r.nextInt(20), "asd",
//                "asd", r.nextDouble(), "asd", Time.getCurTimeStr()));
//        return new Result(true, new ArrayList<>(tmp), "成功");
        List<TimeoutResult> r = this.heuristic.getTors();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/circuitBreakerResult")
    public Result getCircuitBreakerResult() {
        List<CircuitBreakResult> r = this.heuristic.getCbrs();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/bulkheadResult")
    public Result getBulkheadResult() {
        List<BulkHeadResult> r = this.heuristic.getBhrs();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/currentTracePool")
    public Result getCurrentTracePool() {
        List<Trace> r = this.heuristic.getCurrentTracePool();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/tracePool")
    public Result getTracePool() {
        List<Trace> r = this.heuristic.getTracePool();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/currentUrls")
    public Result getCurrentUrls() {
        Set<String> r = this.heuristic.getCurrentUrls();
        if (r == null)
            r = new HashSet<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/IPS")
    public Result getIPS() {
        List<String[]> r = this.heuristic.getIPS();
        if (r == null)
            r = new ArrayList<>();
        return new Result(true, new ArrayList<>(r), "成功");
    }

    @RequestMapping("/test")
    public void test() {
        System.out.println();
    }

    @RequestMapping("/responseTimeGraph")
    public Graph getResponseTimeGraph() {
        return telemetry.queryCurrentResponseTime();
    }

    @RequestMapping("/throughputGraph")
    public Graph getThroughputGraph() {
        return telemetry.queryCurrentThroughoutGraph();
    }

    @RequestMapping("/logFile")
    public Result getExpsLog() {
        String log = "/mylogs/" + this.heuristic.getId() + ".log";
        List<Object> r = new ArrayList<>();
        r.add(log);
        return new Result(true, r, "");
    }
}
