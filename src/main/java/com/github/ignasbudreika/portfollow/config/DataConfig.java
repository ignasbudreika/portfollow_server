package com.github.ignasbudreika.portfollow.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EntityScan(basePackages="com.github.ignasbudreika.portfollow.model")
public class DataConfig {
}
