package press.mizhifei.dentist.clinic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for clinic service.
 * 
 * Configures OAuth2 Resource Server with JWT authentication while allowing
 * public access to specific endpoints like clinic listing.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean springdocEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var authRequests = http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - paths after API Gateway StripPrefix=1
                        // Frontend: /api/clinic/list/all -> Gateway strips /api -> Service receives: /clinic/list/all
                        .requestMatchers("/clinic/list/all").permitAll()
                        .requestMatchers("/clinic/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/clinic/{id:[0-9]+}/patients").permitAll()  // Only GET for specific clinic by ID
                        .requestMatchers(HttpMethod.POST, "/clinic").permitAll()  // Only POST for clinic creation
                        .requestMatchers(HttpMethod.GET, "/clinic/{id:[0-9]+}/dentists").permitAll()  // Public endpoint for clinic dentists
                        .requestMatchers("/actuator/**").permitAll());

        // Only allow OpenAPI endpoints if SpringDoc is enabled (development/docker profiles)
        if (springdocEnabled) {
            authRequests.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll());
        }

        authRequests.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
