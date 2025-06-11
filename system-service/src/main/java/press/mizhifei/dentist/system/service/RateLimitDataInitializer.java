package press.mizhifei.dentist.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import press.mizhifei.dentist.system.model.RateLimitConfig;
import press.mizhifei.dentist.system.repository.RateLimitConfigRepository;

import java.util.List;

/**
 * Data initializer for Rate Limit Configurations
 * 
 * Sets up default rate limiting rules on application startup
 * if no configurations exist in the database.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitDataInitializer implements CommandLineRunner {
    
    private final RateLimitConfigRepository repository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultRateLimitConfigs();
    }
    
    private void initializeDefaultRateLimitConfigs() {
        try {
            long existingCount = repository.count();
            if (existingCount > 0) {
                log.info("Rate limit configurations already exist ({}), skipping initialization", existingCount);
                return;
            }
            
            log.info("Initializing default rate limit configurations...");
            
            List<RateLimitConfig> defaultConfigs = List.of(
                // GenAI Service - Default token-based rate limiting
                RateLimitConfig.builder()
                        .configName("genai-default")
                        .serviceName("genai-service")
                        .endpointPattern("/api/genai/")
                        .maxRequests(10_000L)
                        .timeWindowSeconds(180L) // 3 minutes
                        .limitType(RateLimitConfig.RateLimitType.TOKEN_COUNT)
                        .priority(10)
                        .active(true)
                        .description("Default token-based rate limit for GenAI service (10k tokens per 3 minutes)")
                        .build(),
                
                // GenAI Service - Higher limits for clinic admins
                RateLimitConfig.builder()
                        .configName("genai-clinic-admin")
                        .serviceName("genai-service")
                        .endpointPattern("/api/genai/")
                        .userRole("CLINIC_ADMIN")
                        .maxRequests(25_000L)
                        .timeWindowSeconds(180L) // 3 minutes
                        .limitType(RateLimitConfig.RateLimitType.TOKEN_COUNT)
                        .priority(20)
                        .active(true)
                        .description("Higher token limit for clinic administrators (25k tokens per 3 minutes)")
                        .build(),
                
                // GenAI Service - Higher limits for dentists
                RateLimitConfig.builder()
                        .configName("genai-dentist")
                        .serviceName("genai-service")
                        .endpointPattern("/api/genai/")
                        .userRole("DENTIST")
                        .maxRequests(20_000L)
                        .timeWindowSeconds(180L) // 3 minutes
                        .limitType(RateLimitConfig.RateLimitType.TOKEN_COUNT)
                        .priority(15)
                        .active(true)
                        .description("Higher token limit for dentists (20k tokens per 3 minutes)")
                        .build(),
                
                // Reporting Service - Request-based rate limiting
                RateLimitConfig.builder()
                        .configName("reporting-default")
                        .serviceName("reporting-service")
                        .endpointPattern("/api/reports/")
                        .maxRequests(100L)
                        .timeWindowSeconds(3600L) // 1 hour
                        .limitType(RateLimitConfig.RateLimitType.REQUEST_COUNT)
                        .priority(10)
                        .active(true)
                        .description("Default request limit for reporting service (100 requests per hour)")
                        .build(),
                
                // Chat Log Service - Request-based rate limiting
                RateLimitConfig.builder()
                        .configName("chatlog-default")
                        .serviceName("chat-log-service")
                        .endpointPattern("/api/chatlogs/")
                        .maxRequests(1000L)
                        .timeWindowSeconds(3600L) // 1 hour
                        .limitType(RateLimitConfig.RateLimitType.REQUEST_COUNT)
                        .priority(10)
                        .active(true)
                        .description("Default request limit for chat log service (1000 requests per hour)")
                        .build(),
                
                // System-wide fallback for any unmatched endpoints
                RateLimitConfig.builder()
                        .configName("system-fallback")
                        .serviceName("system")
                        .endpointPattern("/api/")
                        .maxRequests(500L)
                        .timeWindowSeconds(3600L) // 1 hour
                        .limitType(RateLimitConfig.RateLimitType.REQUEST_COUNT)
                        .priority(1) // Lowest priority
                        .active(true)
                        .description("System-wide fallback rate limit (500 requests per hour)")
                        .build()
            );
            
            List<RateLimitConfig> saved = repository.saveAll(defaultConfigs);
            
            log.info("Successfully initialized {} default rate limit configurations:", saved.size());
            for (RateLimitConfig config : saved) {
                log.info("  - {} ({}): {} {} per {} seconds", 
                        config.getConfigName(),
                        config.getEndpointPattern(),
                        config.getMaxRequests(),
                        config.getLimitType().name().toLowerCase().replace("_", " "),
                        config.getTimeWindowSeconds());
            }
            
        } catch (Exception e) {
            log.error("Error initializing default rate limit configurations: {}", e.getMessage(), e);
        }
    }
}
