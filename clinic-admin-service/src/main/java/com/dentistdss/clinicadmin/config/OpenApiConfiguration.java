package com.dentistdss.clinicadmin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * OpenAPI Configuration for Clinic Admin Service
 * 
 * Configures the OpenAPI documentation with proper server URLs to ensure
 * Swagger UI routes API calls through the API Gateway instead of directly
 * to the service.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Configuration
@Profile("!prod") // Only enable for non-production profiles
public class OpenApiConfiguration {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI clinicAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clinic Admin Service API")
                        .description("API for managing clinic administration including clinic CRUD operations, working hours, and holiday management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DentistDSS Development Team")
                                .email("zm377@uowmail.edu.au")
                                .url("https://github.com/zm377"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        // Primary server - API Gateway (for production-like routing)
                        new Server()
                                .url("http://localhost:8080/api/clinic-admin")
                                .description("API Gateway (Recommended)"),
                        // Direct service access (for development/testing)
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct Service Access (Development Only)")
                ));
    }
}
