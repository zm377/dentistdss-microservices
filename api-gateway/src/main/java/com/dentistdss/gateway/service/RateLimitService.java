package com.dentistdss.gateway.service;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dentistdss.gateway.dto.RateLimitConfigDto;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Service for API Gateway
 *
 * Orchestrates rate limiting by coordinating configuration resolution,
 * bucket management, and token estimation. Follows SOLID principles
 * with clear separation of concerns.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitConfigResolver configResolver;
    private final BucketManager bucketManager;
    private final TokenEstimator tokenEstimator;
    
    /**
     * Check if request is allowed based on rate limiting rules
     */
    public Mono<Boolean> isAllowed(String endpoint, String sessionId, String userRole, Long clinicId, long tokens) {
        return Mono.fromCallable(() -> {
            RateLimitConfigDto config = configResolver.resolveConfig(endpoint, userRole, clinicId);
            return checkRateLimit(config, sessionId, tokens);
        }).onErrorResume(error -> {
            log.warn("Error checking rate limit, allowing request: {}", error.getMessage());
            return Mono.just(true); // Allow on error to prevent service disruption
        });
    }
    
    /**
     * Check rate limit using the configuration
     */
    private boolean checkRateLimit(RateLimitConfigDto config, String sessionId, long tokens) {
        Bucket bucket = bucketManager.getBucket(config, sessionId);

        boolean allowed;
        if (config.getLimitType() == RateLimitConfigDto.RateLimitType.TOKEN_COUNT) {
            allowed = bucket.tryConsume(tokens);
        } else {
            allowed = bucket.tryConsume(1); // For request count limiting
        }

        if (!allowed) {
            log.info("Rate limit exceeded for session: {} on endpoint pattern: {}",
                    sessionId, config.getEndpointPattern());
        }

        return allowed;
    }
    
    /**
     * Estimate token consumption for a request
     */
    public long estimateTokens(String endpoint, String content) {
        return tokenEstimator.estimateTokens(endpoint, content);
    }
    
    /**
     * Clear cache (for testing or manual refresh)
     */
    public void clearCache() {
        bucketManager.clearAllBuckets();
        log.info("Cleared rate limit cache");
    }
}
