package com.ada.logginglib.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.Map;
import java.util.Set;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "access")
public class AccessProperties {

    private Set<String> excludedUrls ;
    private Map<String, String> fieldsToMask ;

    public Set<String> getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(Set<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    public Map<String, String> getFieldsToMask() {
        return fieldsToMask;
    }

    public void setFieldsToMask(Map<String, String> fieldsToMask) {
        this.fieldsToMask = fieldsToMask;
    }
}