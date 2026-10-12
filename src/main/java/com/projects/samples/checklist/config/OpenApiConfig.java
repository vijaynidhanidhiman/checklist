package com.projects.samples.checklist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Checklist API")
                        .version("0.0.1-SNAPSHOT")
                        .description("Checklist Application API with Health Monitoring")
                        .contact(new Contact()
                                .name("Checklist Team")
                                .email("support@checklist.example.com")
                                .url("https://github.com/vijaynidhanidhiman/checklist"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.checklist.example.com").description("Production Server")
                ));
    }
}