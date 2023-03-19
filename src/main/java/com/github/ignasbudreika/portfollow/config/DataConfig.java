package com.github.ignasbudreika.portfollow.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories(basePackages="com.github.ignasbudreika.portfollow.repository")
@EnableTransactionManagement
@EntityScan(basePackages="com.github.ignasbudreika.portfollow.model")
public class DataConfig {
}
