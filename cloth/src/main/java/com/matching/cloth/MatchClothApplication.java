package com.matching.cloth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.matching.cloth.repositories")
@EntityScan(basePackages = "com.matching.cloth.models")
@EnableAsync
public class MatchClothApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchClothApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			System.out.println("MatchClothApplication started successfully!");
		};
	}
}
