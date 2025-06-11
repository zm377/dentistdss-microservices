package press.mizhifei.dentist.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import press.mizhifei.dentist.gateway.dto.ApiResponse;
import press.mizhifei.dentist.gateway.dto.RateLimitConfigDto;

/**
 * Rate Limit Configuration Resolver
 * 
 * Responsible for resolving the appropriate rate limit configuration
 * for a given request context. Follows Single Responsibility Principle.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitConfigResolver {
    
    private final RateLimitConfigClient configClient;
    
    // Default rate limit configuration (fallback)
    private static final long DEFAULT_MAX_TOKENS = 10_000;
    private static final long DEFAULT_TIME_WINDOW_SECONDS = 180; // 3 minutes
    
    /**
     * Resolve rate limit configuration for the given context
     */
    @Cacheable(value = "rateLimitConfigs", key = "#endpoint + '_' + #userRole + '_' + #clinicId")
    public RateLimitConfigDto resolveConfig(String endpoint, String userRole, Long clinicId) {
        try {
            ApiResponse<RateLimitConfigDto> response = configClient.findMatchingConfig(endpoint, userRole, clinicId);
            
            if (isValidResponse(response)) {
                log.debug("Resolved rate limit config: {} for endpoint: {}", 
                        response.getData().getConfigName(), endpoint);
                return response.getData();
            } else {
                log.debug("No specific rate limit config found for endpoint: {}, using default", endpoint);
                return createDefaultConfig(endpoint);
            }
        } catch (Exception e) {
            log.warn("Error fetching rate limit config for endpoint: {}, using default: {}", endpoint, e.getMessage());
            return createDefaultConfig(endpoint);
        }
    }
    
    /**
     * Check if the response from config service is valid
     */
    private boolean isValidResponse(ApiResponse<RateLimitConfigDto> response) {
        return response != null && 
               response.isSuccess() && 
               response.getData() != null &&
               response.getData().getActive() != null &&
               response.getData().getActive();
    }
    
    /**
     * Create default rate limit configuration
     */
    private RateLimitConfigDto createDefaultConfig(String endpoint) {
        return RateLimitConfigDto.builder()
                .configName("default")
                .serviceName("default")
                .endpointPattern(endpoint)
                .maxRequests(DEFAULT_MAX_TOKENS)
                .timeWindowSeconds(DEFAULT_TIME_WINDOW_SECONDS)
                .limitType(RateLimitConfigDto.RateLimitType.TOKEN_COUNT)
                .priority(0)
                .active(true)
                .description("Default rate limit configuration")
                .build();
    }
}
