package com.github.ignasbudreika.portfollow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.ignasbudreika.portfollow" })
public class PortfollowApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfollowApplication.class, args);
	}

}
