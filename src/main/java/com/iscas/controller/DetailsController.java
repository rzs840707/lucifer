package com.iscas.controller;

import com.iscas.bean.result.*;
import com.iscas.entity.Result;
import com.iscas.service.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/details/{id}")
public class DetailsController {

    private History history;

    @Autowired
    public DetailsController(History history) {
        this.history = history;
    }

    @RequestMapping("/patternSum")
    public Result getPatternSum(@PathVariable("id") String historyId) {
        Map<String, Integer> counter = new HashMap<>();
        counter.put("timeout", history.queryTimeoutSum(historyId));
        counter.put("retry", history.queryRetrySum(historyId));
        counter.put("circuitbreaker", history.queryCircuitBreakerSum(historyId));
        counter.put("bulkhead", history.queryBulkheadSum(historyId));
        List<Object> data = new ArrayList<>();
        data.add(counter);
        return new Result(true, data, "");
    }

    @RequestMapping("/detectResults")
    public Result getDetectResults(@PathVariable("id") String historyid) {
        List<DetectResult> r = history.queryDetectResults(historyid);
        return new Result(true, new ArrayList<>(r), "");
    }

    @RequestMapping("/timeoutResults")
    public Result getTimeoutResults(@PathVariable("id") String historyid) {
        List<TimeoutResult> r = history.queryTimeoutResults(historyid);
        return new Result(true, new ArrayList<>(r), "");
    }

    @RequestMapping("/retryResults")
    public Result getRetryResults(@PathVariable("id") String historyid) {
        List<RetryResult> r = history.queryRetryResults(historyid);
        return new Result(true, new ArrayList<>(r), "");
    }

    @RequestMapping("/circuitBreakerResults")
    public Result getCircuitBreakerResults(@PathVariable("id") String historyid) {
        List<CircuitBreakResult> r = history.queryCircuitBreakResults(historyid);
        return new Result(true, new ArrayList<>(r), "");
    }

    @RequestMapping("/bulkheadResults")
    public Result getBulkheadResults(@PathVariable("id") String historyid) {
        List<BulkHeadResult> r = history.queryBulkheadResults(historyid);
        return new Result(true, new ArrayList<>(r), "");
    }

    @RequestMapping("/logFile")
    public Result getExpsLog(@PathVariable("id") String historyid) {
        String log = "/mylogs/" + historyid + ".log";
        List<Object> r = new ArrayList<>();
        r.add(log);
        return new Result(true, r, "");
    }

    @RequestMapping("/summary")
    public Result getSummary(@PathVariable("id") String historyid) {
        List<Object> r = new ArrayList<>();
        r.add(history.querySummary(historyid));
        return new Result(true, r, "");
    }
}
