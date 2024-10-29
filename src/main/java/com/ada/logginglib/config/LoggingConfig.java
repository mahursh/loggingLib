package com.ada.logginglib.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {
    private static final ObjectMapper objectMapper = configureObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
