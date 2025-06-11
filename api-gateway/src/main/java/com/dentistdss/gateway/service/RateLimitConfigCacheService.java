package com.dentistdss.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.dentistdss.gateway.dto.ApiResponse;
import com.dentistdss.gateway.dto.RateLimitConfigDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rate Limit Configuration Cache Service
 * 
 * Manages cached rate limit configurations to break the circular dependency
 * between rate limiting components and Feign clients. Loads configurations
 * asynchronously after application startup and refreshes them periodically.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitConfigCacheService {
    
    private final RateLimitConfigClient configClient;
    
    // Thread-safe cache for configurations
    private final Map<String, RateLimitConfigDto> configCache = new ConcurrentHashMap<>();
    private final AtomicBoolean cacheInitialized = new AtomicBoolean(false);
    
    /**
     * Initialize cache after application is fully ready
     * This ensures all beans are created and no circular dependencies occur
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCache() {
        log.info("Initializing rate limit configuration cache...");
        refreshCache();
    }
    
    /**
     * Refresh cache periodically (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void refreshCacheScheduled() {
        if (cacheInitialized.get()) {
            log.debug("Refreshing rate limit configuration cache...");
            refreshCache();
        }
    }
    
    /**
     * Find matching configuration from cache
     */
    public RateLimitConfigDto findMatchingConfig(String endpoint, String userRole, Long clinicId) {
        if (!cacheInitialized.get()) {
            log.warn("Configuration cache not yet initialized, returning null");
            return null;
        }
        
        // Try to find exact match first
        String exactKey = buildCacheKey(endpoint, userRole, clinicId);
        RateLimitConfigDto config = configCache.get(exactKey);
        if (config != null) {
            return config;
        }
        
        // Try to find best matching configuration
        return findBestMatch(endpoint, userRole, clinicId);
    }
    
    /**
     * Refresh the configuration cache
     */
    private void refreshCache() {
        try {
            ApiResponse<List<RateLimitConfigDto>> response = configClient.getActiveConfigurations();
            
            if (isValidResponse(response)) {
                Map<String, RateLimitConfigDto> newCache = new ConcurrentHashMap<>();
                
                for (RateLimitConfigDto config : response.getData()) {
                    if (Boolean.TRUE.equals(config.getActive())) {
                        String key = buildCacheKey(
                            config.getEndpointPattern(), 
                            config.getUserRole(), 
                            config.getClinicId()
                        );
                        newCache.put(key, config);
                    }
                }
                
                // Replace the entire cache atomically
                configCache.clear();
                configCache.putAll(newCache);
                cacheInitialized.set(true);
                
                log.info("Successfully refreshed rate limit configuration cache with {} configurations", 
                        newCache.size());
            } else {
                log.warn("Failed to refresh rate limit configuration cache: invalid response");
            }
        } catch (Exception e) {
            log.error("Error refreshing rate limit configuration cache: {}", e.getMessage(), e);
            // Don't clear the cache on error - keep the last known good configuration
        }
    }
    
    /**
     * Find best matching configuration using pattern matching
     */
    private RateLimitConfigDto findBestMatch(String endpoint, String userRole, Long clinicId) {
        RateLimitConfigDto bestMatch = null;
        int bestScore = -1;
        
        for (RateLimitConfigDto config : configCache.values()) {
            int score = calculateMatchScore(config, endpoint, userRole, clinicId);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = config;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Calculate match score for configuration
     */
    private int calculateMatchScore(RateLimitConfigDto config, String endpoint, String userRole, Long clinicId) {
        int score = 0;
        
        // Endpoint pattern matching (highest priority)
        if (config.getEndpointPattern() != null && endpoint.startsWith(config.getEndpointPattern())) {
            score += 100;
        }
        
        // User role matching
        if (config.getUserRole() != null && config.getUserRole().equals(userRole)) {
            score += 10;
        } else if (config.getUserRole() == null) {
            score += 1; // Generic role config
        }
        
        // Clinic ID matching
        if (config.getClinicId() != null && config.getClinicId().equals(clinicId)) {
            score += 10;
        } else if (config.getClinicId() == null) {
            score += 1; // Generic clinic config
        }
        
        return score;
    }
    
    /**
     * Build cache key for configuration
     */
    private String buildCacheKey(String endpoint, String userRole, Long clinicId) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(endpoint != null ? endpoint : "null")
                  .append(":")
                  .append(userRole != null ? userRole : "null")
                  .append(":")
                  .append(clinicId != null ? clinicId.toString() : "null");
        return keyBuilder.toString();
    }
    
    /**
     * Check if the API response is valid
     */
    private boolean isValidResponse(ApiResponse<List<RateLimitConfigDto>> response) {
        return response != null && 
               response.isSuccess() && 
               response.getData() != null;
    }
    
    /**
     * Get cache status for monitoring
     */
    public boolean isCacheInitialized() {
        return cacheInitialized.get();
    }
    
    /**
     * Get cache size for monitoring
     */
    public int getCacheSize() {
        return configCache.size();
    }
    
    /**
     * Clear cache (for testing or manual refresh)
     */
    public void clearCache() {
        configCache.clear();
        cacheInitialized.set(false);
        log.info("Rate limit configuration cache cleared");
    }
}
