package press.mizhifei.dentist.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

/**
 * Security configuration for API Gateway
 * Centralizes JWT token validation and authentication for all microservices
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean springdocEnabled;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> {
                exchanges
                    // Actuator endpoints
                    .pathMatchers("/actuator/**").permitAll()
                    
                    // Authentication and OAuth2 endpoints
                    .pathMatchers("/api/auth/**").permitAll()
                    .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    
                    // Public clinic endpoints
                    .pathMatchers("/api/clinic/list/all").permitAll()
                    .pathMatchers("/api/clinic/search").permitAll()
                    .pathMatchers("/api/clinic/*/patients").permitAll() // Public GET for specific clinic
                    .pathMatchers("/api/clinic").permitAll() // Public POST for clinic creation
                    .pathMatchers("/api/clinic/*/dentists").permitAll() // Public GET for clinic dentists
                    
                    // Public GenAI endpoints
                    .pathMatchers("/api/genai/chatbot/help").permitAll()
                    .pathMatchers("/api/genai/chatbot/triage").permitAll()
                    .pathMatchers("/api/genai/chatbot/receptionist").permitAll()
                    .pathMatchers("/api/genai/chatbot/aidentist").permitAll()
                    .pathMatchers("/api/genai/chatbot/documentation/summarize").permitAll()
                    
                    // Admin server
                    .pathMatchers("/admin", "/admin/**").permitAll();

                // OpenAPI endpoints (only in development/docker profiles)
                if (springdocEnabled) {
                    exchanges
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers("/*/v3/api-docs", "/*/v3/api-docs/**").permitAll(); // Service-specific docs
                }

                // All other endpoints require authentication
                exchanges.anyExchange().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
