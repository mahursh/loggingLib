package com.ada.logginglib.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomLoggingEncoder extends EncoderBase<ILoggingEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<String> logFields = new ArrayList<>();
    private List<String> jsonKeys = new ArrayList<>();

    @Override
    public byte[] headerBytes() {
        return "".getBytes();
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        Map<String, Object> logData = new LinkedHashMap<>();

        for (String field : logFields) {
            switch (field.trim().toLowerCase()) {
                case "timestamp":
                    logData.put("timestamp", event.getTimeStamp());
                    break;
                case "level":
                    logData.put("level", event.getLevel().toString());
                    break;
                case "logger":
                    logData.put("logger", event.getLoggerName());
                    break;
                case "message":
                    logData.put("message", event.getFormattedMessage());
                    break;
                case "method":
                    logData.put("method", event.getMDCPropertyMap().get("method"));
                    break;
                case "url":
                    logData.put("url", event.getMDCPropertyMap().get("url"));
                    break;
                case "header":
                    logData.put("header", event.getMDCPropertyMap().get("header"));
                    break;
                default:
                    logData.put(field, "unknown");
            }
        }

        try {
            String logString = objectMapper.writeValueAsString(logData);
            return logString.getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error encoding log data", e);
        }
    }

    @Override
    public byte[] footerBytes() {
        return "".getBytes();
    }

}
