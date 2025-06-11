package press.mizhifei.dentist.systemadmin.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * System Administration Service Configuration
 * 
 * Comprehensive configuration for system administration capabilities
 * including caching, async processing, and REST client setup
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Configuration
@EnableCaching
@EnableAsync
public class SystemAdminConfiguration {

    /**
     * Configure Caffeine cache manager for high-performance caching
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure cache with optimal settings for system administration
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats());
        
        // Pre-configure cache names
        cacheManager.setCacheNames(java.util.List.of(
                "rateLimitConfigs",
                "systemParameters",
                "refreshLogs",
                "serviceDiscovery"
        ));
        
        return cacheManager;
    }

    /**
     * Configure REST template for service-to-service communication
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeouts for configuration refresh operations
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "SystemAdminService/1.0");
            request.getHeaders().add("Content-Type", "application/json");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }

    /**
     * Configuration properties for system administration
     */
    @Bean
    public SystemAdminProperties systemAdminProperties() {
        return new SystemAdminProperties();
    }

    /**
     * System Administration Properties
     */
    public static class SystemAdminProperties {
        
        // Default rate limit configurations
        public static final long DEFAULT_CHATBOT_TOKEN_LIMIT = 10000L;
        public static final long DEFAULT_CHATBOT_TIME_WINDOW = 180L; // 3 minutes
        
        // Configuration refresh settings
        public static final int DEFAULT_REFRESH_TIMEOUT_SECONDS = 60;
        public static final int DEFAULT_BATCH_REFRESH_TIMEOUT_SECONDS = 180;
        public static final int DEFAULT_ALL_SERVICES_REFRESH_TIMEOUT_SECONDS = 300;
        
        // Cache settings
        public static final int DEFAULT_CACHE_MAX_SIZE = 1000;
        public static final int DEFAULT_CACHE_EXPIRE_MINUTES = 30;
        
        // Audit settings
        public static final int DEFAULT_LOG_RETENTION_DAYS = 90;
        public static final int DEFAULT_CLEANUP_BATCH_SIZE = 100;
        
        // Security settings
        public static final String SYSTEM_ADMIN_ROLE = "SYSTEM_ADMIN";
        public static final String CLINIC_ADMIN_ROLE = "CLINIC_ADMIN";
        
        // Service endpoints for refresh operations
        public static final java.util.Map<String, String> SERVICE_REFRESH_ENDPOINTS = createServiceEndpointsMap();

        private static java.util.Map<String, String> createServiceEndpointsMap() {
            java.util.Map<String, String> endpoints = new java.util.HashMap<>();
            endpoints.put("api-gateway", "/actuator/refresh");
            endpoints.put("auth-service", "/actuator/refresh");
            endpoints.put("clinic-admin-service", "/actuator/refresh");
            endpoints.put("appointment-service", "/actuator/refresh");
            endpoints.put("clinical-records-service", "/actuator/refresh");
            endpoints.put("user-profile-service", "/actuator/refresh");
            endpoints.put("genai-service", "/actuator/refresh");
            endpoints.put("chat-log-service", "/actuator/refresh");
            endpoints.put("reporting-service", "/actuator/refresh");
            endpoints.put("audit-service", "/actuator/refresh");
            endpoints.put("notification-service", "/actuator/refresh");
            return java.util.Collections.unmodifiableMap(endpoints);
        }
        
        // Default system parameters
        public static final java.util.Map<String, Object> DEFAULT_SYSTEM_PARAMETERS = java.util.Map.of(
            "ai.model.temperature", 0.7,
            "ai.model.max_tokens", 2000,
            "ai.model.name", "gpt-3.5-turbo",
            "email.notification.enabled", true,
            "file.upload.max_size_mb", 10,
            "session.timeout_minutes", 30,
            "audit.log.level", "INFO",
            "audit.retention_days", 90
        );
    }
}
