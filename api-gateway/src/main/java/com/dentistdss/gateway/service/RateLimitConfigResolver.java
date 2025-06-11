package com.dentistdss.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.dentistdss.gateway.dto.RateLimitConfigDto;

/**
 * Rate Limit Configuration Resolver
 *
 * Responsible for resolving the appropriate rate limit configuration
 * for a given request context. Uses cached configurations to avoid
 * circular dependencies with Feign clients.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitConfigResolver {

    private final RateLimitConfigCacheService configCacheService;

    // Default rate limit configuration (fallback)
    private static final long DEFAULT_MAX_TOKENS = 10_000;
    private static final long DEFAULT_TIME_WINDOW_SECONDS = 180; // 3 minutes

    /**
     * Resolve rate limit configuration for the given context
     */
    public RateLimitConfigDto resolveConfig(String endpoint, String userRole, Long clinicId) {
        try {
            RateLimitConfigDto config = configCacheService.findMatchingConfig(endpoint, userRole, clinicId);

            if (config != null && Boolean.TRUE.equals(config.getActive())) {
                log.debug("Resolved rate limit config: {} for endpoint: {}",
                        config.getConfigName(), endpoint);
                return config;
            } else {
                log.debug("No specific rate limit config found for endpoint: {}, using default", endpoint);
                return createDefaultConfig(endpoint);
            }
        } catch (Exception e) {
            log.warn("Error resolving rate limit config for endpoint: {}, using default. Error: {}",
                    endpoint, e.getMessage());
            return createDefaultConfig(endpoint);
        }
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
