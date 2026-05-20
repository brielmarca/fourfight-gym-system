package com.gym.config;

import java.util.Arrays;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Gym Management Backend}")
    private String appName;

    @Bean
    public OpenAPI openAPI() {
        log.info("[STARTUP] START SwaggerConfig.openAPI creation");
        OpenAPI api = new OpenAPI()
            .info(new Info()
                .title(appName)
                .version("1.0.0")
                .description("Production-grade Gym Management System API"))
            .addServersItem(new Server().url("/").description("Current server"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT access token (Bearer token)")))
            .security(Arrays.asList(new SecurityRequirement().addList("bearerAuth")));
        log.info("[STARTUP] END SwaggerConfig.openAPI creation");
        return api;
    }
}