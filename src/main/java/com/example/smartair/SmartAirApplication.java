package com.example.smartair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class SmartAirApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartAirApplication.class, args);
	}

}
