package com.iscas.controller;

import com.iscas.entity.Result;
import com.iscas.service.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private History history;

    @Autowired
    public HistoryController(History history) {
        this.history = history;
    }

    @RequestMapping("/summaries")
    public Result getSummaries() {
        return new Result(true, new ArrayList<>(history.findAllSummaries()), "");
    }

    @RequestMapping("/deleteSummary")
    public Result deleteSummary(String id) {
        this.history.deleteSummaryById(id);
        return new Result(true, null, null);
    }
}
