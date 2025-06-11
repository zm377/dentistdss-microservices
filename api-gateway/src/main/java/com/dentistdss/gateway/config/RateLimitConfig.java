package com.dentistdss.gateway.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Rate Limiting Configuration
 * 
 * Configures beans required for rate limiting functionality.
 * This configuration ensures proper bean ordering and avoids circular dependencies.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Configuration
@EnableCaching
public class RateLimitConfig {
    
    /**
     * RestTemplate bean specifically for rate limit configuration service
     * This bean is created early in the application context to avoid circular dependencies
     */
    @Bean("rateLimitRestTemplate")
    public RestTemplate rateLimitRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(10000);   // 10 seconds

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
