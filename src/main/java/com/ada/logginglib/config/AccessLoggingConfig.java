package com.ada.logginglib.config;

import com.ada.logginglib.filter.CustomAccessLogFilter;
import com.ada.logginglib.prop.AccessProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(AccessProperties.class)
@Import({ObjectMapperConfig.class})
public class AccessLoggingConfig {

    @Bean
    @ConditionalOnMissingBean
    public CustomAccessLogFilter accessLogFilter(ObjectMapper objectMapper , AccessProperties accessProperties){
        CustomAccessLogFilter accessLogFilter = new CustomAccessLogFilter(objectMapper);
        accessLogFilter.setExcludedUrls(accessProperties.getExcludedUrls());
        accessLogFilter.setFieldsToMask(accessProperties.getFieldsToMask());
        return accessLogFilter;
    }

}
