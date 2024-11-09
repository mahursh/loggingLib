package com.ada.logginglib.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ada.logginglib.utils.JsonUtil;


import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import static com.ada.logginglib.constant.Colors.*;

//TODO: https://www.geeksforgeeks.org/get-the-response-body-in-spring-boot-filter/ .
//TODO: https://www.appsdeveloperblog.com/read-body-from-httpservletrequest-in-spring-filter/ .
//TODO: https://gist.github.com/thegeekyasian/c21bb407827b32d2d9cb787f4cf72e3a .
//TODO: remove all methods from here and put them to the seperated files .
//TODO: add try catch if needed .
//TODO: test AutoConfiguration .
//TODO: masking only applies on JSON format .
//TODO: problem ---> dose not work on reactive microservice .

//@Component
public class LoggingFilter implements Filter {

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private Set<Pattern> ignoredPaths = new HashSet<>();
    private Set<Pattern> excludedBodyPaths = new HashSet<>();
    private String logFormat = "JSON";

    private Map<Pattern, String> maskingRules = new HashMap<>();

    public LoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setIgnoredPaths(Set<String> ignoredPaths) {
        this.ignoredPaths = compilePatterns(ignoredPaths);
    }

    public void setExcludedBodyPaths(Set<String> excludedBodyPaths) {
        this.excludedBodyPaths = compilePatterns(excludedBodyPaths);
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }


    public void setMaskingRules(Map<String, String> rules) {
        rules.forEach((fieldOrPattern, mask) -> {

            if (fieldOrPattern.matches(".*[\\[\\]().*+?^$\\\\].*") || fieldOrPattern.startsWith("(?i)")) {
                maskingRules.put(Pattern.compile(fieldOrPattern), mask);
            } else {
                maskingRules.put(Pattern.compile("(?i)" + fieldOrPattern), mask);
            }
        });
    }


    private String applyMaskingRules(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            maskFields(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (IOException e) {
            logger.error("There Was An Error In Masking Fields !");
            return jsonString;
        }
    }

    private void maskFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for (Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                for (Map.Entry<Pattern, String> rule : maskingRules.entrySet()) {
                    if (rule.getKey().matcher(fieldName).matches() && fieldValue.isTextual()) {
                        objectNode.put(fieldName, rule.getValue());
                    }
                }

                maskFields(fieldValue);
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                maskFields(arrayItem);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;


        if (shouldLog(httpServletRequest.getRequestURI())) {
            var start = Instant.now();
            logEntry(httpServletRequest);

            chain.doFilter(request, response);

            var end = Instant.now();
            logExit(httpServletRequest, httpServletResponse, start, end);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean shouldLog(String requestUri) {
        return ignoredPaths.stream().noneMatch(pattern -> pattern.matcher(requestUri).matches());
    }

    private void logEntry(HttpServletRequest request) throws IOException {
        String body = getRequestBody(request);
        String headersFormatted = JsonUtil.formatHeaders(JsonUtil.headersToMap(request));

        if ("JSON".equalsIgnoreCase(logFormat)) {

            var logEntry = new LogEntry(request.getRequestURI(), request.getMethod(), headersFormatted, body);
            String jsonLogEntry = objectMapper.writeValueAsString(logEntry);
            jsonLogEntry = applyMaskingRules(jsonLogEntry);
            logger.info(jsonLogEntry);


        } else if ("ISO".equalsIgnoreCase(logFormat)){
            String isoLogEntry = String.format(COLOR_GREEN + """
                    \n
                    REQUEST:
                    \tURL: %s
                    \tHTTP METHOD: %s
                    \tHEADERS: \n %s
                    \tBODY: %s
                    """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    headersFormatted,
                    body

                    );
            logger.info(isoLogEntry);

        }//TODO: handle other formats and not valid formats.
    }

    private void logExit(HttpServletRequest request, HttpServletResponse response, Instant start, Instant end) throws JsonProcessingException {
        String duration = String.format("%d ms", ChronoUnit.MILLIS.between(start, end));

        if ("JSON".equalsIgnoreCase(logFormat)) {
            var logExit = new LogExit(request.getRequestURI(), request.getMethod(), response.getStatus(), duration);
            String jsonLogExit = objectMapper.writeValueAsString(logExit);

            jsonLogExit = applyMaskingRules(jsonLogExit);

            logger.info(jsonLogExit);
        }
        else if ("ISO".equalsIgnoreCase(logFormat)) {
            String isoLogExit = String.format(COLOR_BLUE + """
            \n
            RESPONSE:
            \tURL: %s
            \tHTTP METHOD: %s
            \tHTTP STATUS: %d
            \tDURATION: %s
            """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    duration
            );
            logger.info(isoLogExit);
        } // TODO: handle other formats and invalid formats if needed.
    }


    private String getRequestBody(HttpServletRequest request) throws IOException {
        if (excludedBodyPaths.stream().anyMatch(pattern -> pattern.matcher(request.getRequestURI()).matches())) {
            return "Body excluded";
        }
        // TODO: logic to read the body .

        return "";
    }

    private Set<Pattern> compilePatterns(Set<String> regexStrings) {
        Set<Pattern> patterns = new HashSet<>();
        for (String regex : regexStrings) {
            patterns.add(Pattern.compile(regex));
        }
        return patterns;
    }


    @Override
    public void destroy() {}
    //TODO: a method to flatten the nested object.


    private static class LogEntry {
        private final String url;
        private final String method;
        private final String headers;
        private final String body;

        public LogEntry(String url, String method, String headers, String body) {
            this.url = url;
            this.method = method;
            this.headers = headers;
            this.body = body;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public String getHeaders() { return headers; }
        public String getBody() { return body; }
    }

    private static class LogExit {
        private final String url;
        private final String method;
        private final int status;
        private final String duration;

        public LogExit(String url, String method, int status, String duration) {
            this.url = url;
            this.method = method;
            this.status = status;
            this.duration = duration;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public int getStatus() { return status; }
        public String getDuration() { return duration; }
    }
}
