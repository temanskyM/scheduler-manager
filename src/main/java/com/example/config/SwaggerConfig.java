package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI schedulerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Schedule Manager API")
                        .description(
                                "API for managing school schedules, including teachers, students, subjects, and classrooms")
                        .version("1.0"));
    }
} 