package com.ada.logginglib.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;


@ConfigurationProperties(prefix = "logginglib")
public class LoggingProperties {

    private Set<String> ignoredPaths;


    private Set<String> excludedBodyPaths;


    private String logFormat = "JSON";


    private Map<String, String> maskingRules;


    public Set<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public void setIgnoredPaths(Set<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
    }

    public Set<String> getExcludedBodyPaths() {
        return excludedBodyPaths;
    }

    public void setExcludedBodyPaths(Set<String> excludedBodyPaths) {
        this.excludedBodyPaths = excludedBodyPaths;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    public Map<String, String> getMaskingRules() {
        return maskingRules;
    }

    public void setMaskingRules(Map<String, String> maskingRules) {
        this.maskingRules = maskingRules;
    }
}
