package com.ada.logginglib.filter;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CustomAccessLogFilter implements Filter {



    private Set<String> excludedUrls = new HashSet<>();
    private Map<String, String> fieldsToMask = new HashMap<>();
    private ObjectMapper objectMapper;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("ACCESS");

    public CustomAccessLogFilter(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    public void setExcludedUrls(Set<String> excludedUrls){
        this.excludedUrls = excludedUrls;
    }
    public void setFieldsToMask(Map<String, String> fieldsToMask){
        this.fieldsToMask = fieldsToMask;
    }





    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;


            String requestUrl = httpRequest.getRequestURI();
            if (!excludedUrls.contains(requestUrl)) {

                LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
                long requestTimestamp = System.currentTimeMillis();
                filterChain.doFilter(request, response);
                long elapsedTime = System.currentTimeMillis() - requestTimestamp;

                AccessEvent accessEvent = new AccessEvent(context, httpRequest, httpResponse, new ServerAdapter() {

                    @Override
                    public long getRequestTimestamp() {
                        return requestTimestamp;
                    }

                    @Override
                    public long getContentLength() {
                        return 0;
                    }

                    @Override
                    public int getStatusCode() {
                        return httpResponse.getStatus();
                    }

                    @Override
                    public Map<String, String> buildResponseHeaderMap() {
                        Map<String, String> headers = new HashMap<>();
                        for (String headerName : httpResponse.getHeaderNames()) {
                            headers.put(headerName, httpResponse.getHeader(headerName));
                        }
                        return headers;
                    }


                });

                maskSensitiveFields(httpRequest, httpResponse);

                logAccessEvent(accessEvent, elapsedTime);
            }
        }

    }

    private void maskSensitiveFields(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        for (Map.Entry<String, String> entry : fieldsToMask.entrySet()) {
            String fieldName = entry.getKey();
            String maskPattern = entry.getValue();


            String[] paramValues = httpRequest.getParameterValues(fieldName);
            if (paramValues != null) {
                for (int i = 0; i < paramValues.length; i++) {
                    paramValues[i] = maskPattern;
                }
            }


            String headerValue = httpRequest.getHeader(fieldName);
            if (headerValue != null) {
                httpRequest.setAttribute(fieldName, maskPattern);
            }


            String responseHeaderValue = httpResponse.getHeader(fieldName);
            if (responseHeaderValue != null) {
                httpResponse.setHeader(fieldName, maskPattern);
            }
        }
    }

    private void logAccessEvent(AccessEvent accessEvent, long elapsedTime) {
        logger.info("AccessEvent: {}, elapsedTime: {} ms", accessEvent, elapsedTime);
    }
}
