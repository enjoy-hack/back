package com.example.smartair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class EnjoyBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnjoyBackApplication.class, args);
	}

}
