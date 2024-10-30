package com.ada.logginglib.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.ada.logginglib.utils.JsonUtil;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static com.ada.logginglib.constant.Colors.COLOR_GREEN;
import static com.ada.logginglib.constant.Colors.COLOR_RESET;
import static com.ada.logginglib.constant.Colors.COLOR_BLUE;

@Component

public class LoggingFilter implements Filter {

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);


    private String logFormat = "JSON";



    public LoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;


        var start = Instant.now();


        logEntry(httpServletRequest);


        chain.doFilter(request, response);


        var end = Instant.now();
        logExit(httpServletRequest, httpServletResponse, start, end);
    }

    private void logEntry(HttpServletRequest request) {
        String entryLog;
        String headersFormatted = JsonUtil.formatHeaders(JsonUtil.headersToMap(request)); // Use the new method

        if ("ISO27001".equalsIgnoreCase(logFormat)) {
            entryLog = String.format(COLOR_BLUE + """
                \n<-----------------------------------ISO 27001 SERVICE CALL----------------------------------->
                REQUEST:
                \tURL: %s
                \tHTTP METHOD: %s
                \tHEADER:
                %s
                <-------------------------------------------------------------------------------------------->
                """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    headersFormatted
            );
        } else {
            entryLog = String.format(COLOR_GREEN + """
                \n<----------------------------------------JSON SERVICE CALL---------------------------------------->
                REQUEST:
                \tURL: %s
                \tHTTP METHOD: %s
                \tHEADER:
                %s
                <------------------------------------------------------------------------------------------------>
                """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    headersFormatted
            );
        }
        logger.info(entryLog);
    }

    private void logExit(HttpServletRequest request, HttpServletResponse response, Instant start, Instant end) {
        String duration = String.format("%d ms", ChronoUnit.MILLIS.between(start, end));
        String exitLog;

        if ("ISO27001".equalsIgnoreCase(logFormat)) {
            exitLog = String.format(COLOR_BLUE + """
                \n<-----------------------------------ISO 27001 SERVICE END----------------------------------->
                RESPONSE:
                \tURL: %s
                \tHTTP METHOD: %s
                \tHTTP STATUS: %d
                \tDURATION: %s
                <------------------------------------------------------------------------------------------->
                """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    duration
            );
        } else {
            exitLog = String.format(COLOR_GREEN + """
                \n<----------------------------------------JSON SERVICE END---------------------------------------->
                RESPONSE:
                \tURL: %s
                \tHTTP METHOD: %s
                \tHTTP STATUS: %d
                \tDURATION: %s
                <------------------------------------------------------------------------------------------------>
                """ + COLOR_RESET,
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    duration
            );
        }
        logger.info(exitLog);
    }

    private void logException(Exception exception, ResponseEntity<?> response) {
        String message = "\n<---------------------------------------------EXCEPTION SERVICE CALLED---------------------------------------------->\n";
        message += String.format("MESSAGE: %s\n", exception.getMessage());
        message += String.format("RESPONSE: \n\tBODY: %s\n", JsonUtil.prettyJson(response.getBody(), objectMapper));
        message += String.format("\tHTTP STATUS: %s\n", response.getStatusCode());
        message += "<-------------------------------------------------------------------------------------------------------------------\n>";

        logger.error(message);
    }
}

