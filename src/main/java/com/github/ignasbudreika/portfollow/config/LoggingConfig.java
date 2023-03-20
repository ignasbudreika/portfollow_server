package com.github.ignasbudreika.portfollow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.json.JsonBodyFilters;

import java.util.Set;

@Configuration
public class LoggingConfig {
    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .bodyFilter(JsonBodyFilters.replaceJsonStringProperty(Set.of("password", "client_secret"), "xxx")).build();
    }
}
