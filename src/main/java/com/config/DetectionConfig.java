package com.iscas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DetectionConfig {
    @Bean
    public com.iscas.bean.Configuration getDefaultConfigration() {
        return new com.iscas.bean.Configuration();
    }
}
