package com.iscas.service;

import com.iscas.bean.result.*;
import com.iscas.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class History {

    private DetectResultDAO detectResultDAO;
    private TimeoutResultDAO timeoutResultDAO;
    private RetryResultDAO retryResultDAO;
    private CircuitBreakerResultDAO circuitBreakerResultDAO;
    private BulkHeadResultDAO bulkHeadResultDAO;
    private SummaryDAO summaryDAO;


    @Autowired
    public History(DetectResultDAO detectResultDAO,
                   TimeoutResultDAO timeoutResultDAO,
                   RetryResultDAO retryResultDAO,
                   CircuitBreakerResultDAO circuitBreakerResultDAO,
                   BulkHeadResultDAO bulkHeadResultDAO,
                   SummaryDAO summaryDAO) {
        this.detectResultDAO = detectResultDAO;
        this.timeoutResultDAO = timeoutResultDAO;
        this.retryResultDAO = retryResultDAO;
        this.circuitBreakerResultDAO = circuitBreakerResultDAO;
        this.bulkHeadResultDAO = bulkHeadResultDAO;
        this.summaryDAO = summaryDAO;
    }

    public List<DetectResult> queryDetectResults(String id) {
        return this.detectResultDAO.findAllById(id);
    }

    public List<TimeoutResult> queryTimeoutResults(String id) {
        return this.timeoutResultDAO.findAllById(id);
    }

    public List<RetryResult> queryRetryResults(String id) {
        return this.retryResultDAO.findAllById(id);
    }

    public List<CircuitBreakResult> queryCircuitBreakResults(String id) {
        return this.circuitBreakerResultDAO.findAllById(id);
    }

    public List<BulkHeadResult> queryBulkheadResults(String id) {
        return this.bulkHeadResultDAO.findAllById(id);
    }

    public int queryTimeoutSum(String id) {
        return this.timeoutResultDAO.countAllById(id);
    }

    public int queryRetrySum(String id) {
        return this.retryResultDAO.countAllById(id);
    }

    public int queryBulkheadSum(String id) {
        return this.bulkHeadResultDAO.countAllById(id);
    }

    public int queryCircuitBreakerSum(String id) {
        return this.circuitBreakerResultDAO.countAllById(id);
    }

    public Summary querySummary(String id) {
        return this.summaryDAO.findById(id);
    }

    public List<Summary> findAllSummaries() {
        return this.summaryDAO.findAll();
    }

    @Transactional
    public void deleteSummaryById(String id) {
        this.summaryDAO.deleteById(id);
    }
}
