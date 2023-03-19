package com.github.ignasbudreika.portfollow.external.config;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfiguration {
    @Bean("unwrapped")
    public ObjectMapper unwrappedObjectMapper() {
        return objectMapper(true);
    }

    @Bean("wrapped")
    public ObjectMapper wrappedObjectMapper() {
        return objectMapper(false);
    }

    private ObjectMapper objectMapper(boolean unwrap) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, unwrap);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationConfig.Feature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        return objectMapper;
    }
}
