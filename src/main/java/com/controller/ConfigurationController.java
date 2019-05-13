package com.iscas.controller;

import com.iscas.bean.Configuration;
import com.iscas.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    private Configuration configuration;

    @Autowired
    public ConfigurationController(Configuration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public Result update(@RequestBody Configuration config) {
        set(config);
        return new Result(true, null, "成功更新");
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public Result query() {
        List<Object> r = new ArrayList<>();
        r.add(this.configuration);
        return new Result(true, r, "");
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public Result reset() {
        Configuration f = new Configuration();
        set(f);
        return new Result(true, null, "");
    }

    private void set(Configuration newConfig) {
        // 更新所有的值
        configuration.setTraceDetectDuration(newConfig.getTraceDetectDuration());
        configuration.setTraceDetectSampleInterval(newConfig.getTraceDetectSampleInterval());
        configuration.setTraceDetectSampleBatch(newConfig.getTraceDetectSampleBatch());

        configuration.setTimeoutDetectionDuration(newConfig.getTimeoutDetectionDuration());
        configuration.setTimeoutSampleBatch(newConfig.getTimeoutSampleBatch());
        configuration.setTimeoutSampleInterval(newConfig.getTimeoutSampleInterval());

        configuration.setRetryDetectionDuration(newConfig.getRetryDetectionDuration());
        configuration.setRetrySampleBatch(newConfig.getRetrySampleBatch());
        configuration.setRetrySampleInterval(newConfig.getRetrySampleInterval());

        configuration.setCircuitbreakerLocateDuration(newConfig.getCircuitbreakerLocateDuration());
        configuration.setCircuitbreakerLocateSampleBatch(newConfig.getCircuitbreakerLocateSampleBatch());
        configuration.setCircuitbreakerLocateInterval(newConfig.getCircuitbreakerLocateInterval());
        configuration.setCircuitbreakerHalfStateDuration(newConfig.getCircuitbreakerHalfStateDuration());
        configuration.setCircuitbreakerInterval(newConfig.getCircuitbreakerInterval());
        configuration.setCircuitbreakerQPSWindowSize(newConfig.getCircuitbreakerQPSWindowSize());

        configuration.setBulkheadDuration1(newConfig.getBulkheadDuration1());
        configuration.setBulkheadInterval1(newConfig.getBulkheadInterval1());
        configuration.setBulkheadWindowSize1(newConfig.getBulkheadWindowSize1());
        configuration.setBulkheadDuration2(newConfig.getBulkheadDuration2());
        configuration.setBulkheadInterval2(newConfig.getBulkheadInterval2());
        configuration.setBulkheadWindowSize2(newConfig.getBulkheadWindowSize2());
        configuration.setBulkheadThreshold(newConfig.getBulkheadThreshold());
    }
}
