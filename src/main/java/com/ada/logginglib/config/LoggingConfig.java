package com.ada.logginglib.config;


import com.ada.logginglib.filter.LoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
public class LoggingConfig {
    @Bean
    @ConditionalOnMissingBean
    @DependsOn("objectMapper")
    public LoggingFilter loggingFilter(ObjectMapper objectMapper) {
        return new LoggingFilter(objectMapper);
    }


}
