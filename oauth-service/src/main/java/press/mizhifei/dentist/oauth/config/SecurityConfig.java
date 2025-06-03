package press.mizhifei.dentist.oauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import press.mizhifei.dentist.oauth.security.OAuth2LoginSuccessHandler;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    // private final CustomOAuth2UserService customOAuth2UserService; // If using custom user service

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean springdocEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var authRequests = http
            // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/actuator/**").permitAll());

        // Only allow OpenAPI endpoints if SpringDoc is enabled (development/docker profiles)
        if (springdocEnabled) {
            authRequests.authorizeHttpRequests(authz -> authz
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll());
        }

        authRequests.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
            .oauth2Login(oauth2Login -> oauth2Login
                // .userInfoEndpoint(userInfo -> userInfo
                //     .userService(customOAuth2UserService) // If custom user service needed
                // )
                .successHandler(oAuth2LoginSuccessHandler)
            );
        return http.build();
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     // This should allow your frontend to make requests to the /oauth2/authorization/google endpoint
    //     configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://dentist.mizhifei.press", "https://accounts.google.com")); 
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     configuration.setAllowedHeaders(Arrays.asList("*"));
    //     configuration.setAllowCredentials(true);
        
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }
} 