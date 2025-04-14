package com.example.smartair.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean // 전역 사용을 위해 Bean 등록
    public WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }
}
