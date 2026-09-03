package com.afristock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.afristock.repository")
public class AfristockApplication {

	public static void main(String[] args) {
		SpringApplication.run(AfristockApplication.class, args);
	}

}
