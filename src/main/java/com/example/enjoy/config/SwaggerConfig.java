package com.example.enjoy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // API Key Scheme 정의
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("인증을 위한 토큰");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("ApiKeyAuth");

        // 서버 정의
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Server");

        Server apiServer = new Server();
        apiServer.setUrl("http://3.36.34.67:8080");
        apiServer.setDescription("API Server");

        return new OpenAPI()
                .info(new Info().title("Enjoy Hack API")
                        .description("EnjoyHack Application API Documentation")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("ApiKeyAuth", apiKeyScheme)
                .servers(List.of(localServer, apiServer));    // 로컬서버와 운영서버 모두 등록
    }
}