package press.mizhifei.dentist.gateway.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import press.mizhifei.dentist.gateway.dto.RateLimitConfigDto;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bucket Manager for Rate Limiting
 * 
 * Manages the creation and lifecycle of rate limiting buckets.
 * Follows Single Responsibility Principle by focusing only on bucket management.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
public class BucketManager {
    
    // Local cache for buckets (in production, consider Redis-based distributed buckets)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Get or create a bucket for the given configuration and session
     */
    public Bucket getBucket(RateLimitConfigDto config, String sessionId) {
        String bucketKey = generateBucketKey(config, sessionId);
        return buckets.computeIfAbsent(bucketKey, key -> createBucket(config));
    }
    
    /**
     * Create a new bucket based on configuration
     */
    private Bucket createBucket(RateLimitConfigDto config) {
        Duration refillDuration = Duration.ofSeconds(config.getTimeWindowSeconds());
        
        Bandwidth bandwidth = Bandwidth.classic(
                config.getMaxRequests(),
                Refill.greedy(config.getMaxRequests(), refillDuration)
        );
        
        Bucket bucket = Bucket.builder()
                .addLimit(bandwidth)
                .build();
        
        log.debug("Created new bucket with limit: {} {} per {} seconds", 
                config.getMaxRequests(), 
                config.getLimitType().name().toLowerCase(),
                config.getTimeWindowSeconds());
        
        return bucket;
    }
    
    /**
     * Generate unique bucket key for the configuration and session
     */
    private String generateBucketKey(RateLimitConfigDto config, String sessionId) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:")
                  .append(config.getConfigName())
                  .append(":")
                  .append(sessionId);
        
        // Add role and clinic specificity if configured
        if (config.getUserRole() != null) {
            keyBuilder.append(":role:").append(config.getUserRole());
        }
        if (config.getClinicId() != null) {
            keyBuilder.append(":clinic:").append(config.getClinicId());
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * Clear all buckets (for testing or manual refresh)
     */
    public void clearAllBuckets() {
        int size = buckets.size();
        buckets.clear();
        log.info("Cleared {} rate limit buckets", size);
    }
    
    /**
     * Get current bucket count (for monitoring)
     */
    public int getBucketCount() {
        return buckets.size();
    }
}
