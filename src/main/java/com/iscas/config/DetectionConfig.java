package com.iscas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DetectionConfig {
    @Bean
    public com.iscas.bean.Configuration getDefaultConfigration() {
        com.iscas.bean.Configuration configuration = new com.iscas.bean.Configuration();

        // default configuration
        configuration.setTraceDetectDuration(120); // 120秒
        configuration.setTraceDetectSampleBatch(20); // 20条
        configuration.setTraceDetectSampleInterval(1); // 1次

        return configuration;
    }
}
