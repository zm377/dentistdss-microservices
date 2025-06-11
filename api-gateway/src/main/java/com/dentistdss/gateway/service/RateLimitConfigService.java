package com.dentistdss.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.dentistdss.gateway.dto.ApiResponse;
import com.dentistdss.gateway.dto.RateLimitConfigDto;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Rate Limit Configuration Service
 * 
 * Handles fetching rate limit configurations from the system-admin-service
 * using RestTemplate to avoid circular dependency with Feign clients.
 * This service bypasses the gateway's routing to directly call the service.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
public class RateLimitConfigService {
    
    private final RestTemplate restTemplate;
    private final String serviceUrl;
    
    // Default rate limit configuration (fallback)
    private static final long DEFAULT_MAX_TOKENS = 10_000;
    private static final long DEFAULT_TIME_WINDOW_SECONDS = 180; // 3 minutes
    
    public RateLimitConfigService(
            @Qualifier("rateLimitRestTemplate") RestTemplate restTemplate,
            @Value("${rate-limit.config.service-url:http://system-admin-service:8086}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }
    
    @PostConstruct
    public void init() {
        log.info("RateLimitConfigService initialized with service URL: {}", serviceUrl);
    }
    
    /**
     * Find matching rate limit configuration for the given context
     */
    @Cacheable(value = "rateLimitConfigs", key = "#endpoint + '_' + #userRole + '_' + #clinicId")
    public RateLimitConfigDto findMatchingConfig(String endpoint, String userRole, Long clinicId) {
        try {
            String url = buildMatchingConfigUrl(endpoint, userRole, clinicId);
            log.debug("Fetching rate limit config from: {}", url);
            
            @SuppressWarnings("unchecked")
            ApiResponse<RateLimitConfigDto> response = restTemplate.getForObject(url, ApiResponse.class);
            
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
     * Get all active rate limit configurations
     */
    @Cacheable(value = "allRateLimitConfigs")
    public List<RateLimitConfigDto> getActiveConfigurations() {
        try {
            String url = serviceUrl + "/api/system-admin/config/rate-limits/active";
            log.debug("Fetching all active rate limit configs from: {}", url);
            
            @SuppressWarnings("unchecked")
            ApiResponse<List<RateLimitConfigDto>> response = restTemplate.getForObject(url, ApiResponse.class);
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("Failed to fetch active rate limit configurations");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching active rate limit configurations: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Build URL for matching configuration request
     */
    private String buildMatchingConfigUrl(String endpoint, String userRole, Long clinicId) {
        StringBuilder urlBuilder = new StringBuilder(serviceUrl)
                .append("/api/system-admin/config/rate-limits/match")
                .append("?endpoint=").append(endpoint);
        
        if (userRole != null && !userRole.trim().isEmpty()) {
            urlBuilder.append("&userRole=").append(userRole);
        }
        
        if (clinicId != null) {
            urlBuilder.append("&clinicId=").append(clinicId);
        }
        
        return urlBuilder.toString();
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
