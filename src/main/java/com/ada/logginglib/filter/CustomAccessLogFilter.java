package com.ada.logginglib.filter;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.classic.LoggerContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomAccessLogFilter implements Filter {

    private final List<String> excludedUrls;
    private final Map<String, String> fieldsToMask;

    public CustomAccessLogFilter(List<String> excludedUrls, Map<String, String> fieldsToMask) {
        this.excludedUrls = excludedUrls;
        this.fieldsToMask = fieldsToMask;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;


            String requestUrl = httpRequest.getRequestURI();
            if (excludedUrls.contains(requestUrl)) {

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
        // Iterate through the fields to mask
        for (Map.Entry<String, String> entry : fieldsToMask.entrySet()) {
            String fieldName = entry.getKey();
            String maskPattern = entry.getValue();

            // Mask the request parameters
            String[] paramValues = httpRequest.getParameterValues(fieldName);
            if (paramValues != null) {
                for (int i = 0; i < paramValues.length; i++) {
                    httpRequest.setAttribute(fieldName, paramValues[i].replaceAll(".", maskPattern));
                }
            }

            // Mask headers if necessary
            String headerValue = httpRequest.getHeader(fieldName);
            if (headerValue != null) {
                httpRequest.setAttribute(fieldName, headerValue.replaceAll(".", maskPattern));
            }

            // Mask response headers if necessary
            String responseHeaderValue = httpResponse.getHeader(fieldName);
            if (responseHeaderValue != null) {
                httpResponse.setHeader(fieldName, responseHeaderValue.replaceAll(".", maskPattern));
            }
        }
    }

    private void logAccessEvent(AccessEvent accessEvent, long elapsedTime) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("ACCESS");
        // Log the AccessEvent with the additional elapsedTime
        logger.info("AccessEvent: {}, elapsedTime: {} ms", accessEvent, elapsedTime);
    }
}
