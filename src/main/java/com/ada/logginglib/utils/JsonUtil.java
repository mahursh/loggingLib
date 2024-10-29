package com.ada.logginglib.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {


    public static String prettyJson(Object json, ObjectMapper objectMapper) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Map<String, String> headersToMap(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headersMap.put(headerName, request.getHeader(headerName));
        }
        return headersMap;
    }
}


