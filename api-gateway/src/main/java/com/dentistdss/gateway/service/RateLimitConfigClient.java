package com.dentistdss.gateway.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.dentistdss.gateway.dto.ApiResponse;
import com.dentistdss.gateway.dto.RateLimitConfigDto;

import java.util.List;

/**
 * Feign client for retrieving rate limit configurations from system service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@FeignClient(name = "system-admin-service", path = "/api/system-admin/config/rate-limits", url = "${rate-limit.config.service-url:http://system-admin-service:8086}")
public interface RateLimitConfigClient {
    
    /**
     * Get all active rate limit configurations
     */
    @GetMapping("/active")
    ApiResponse<List<RateLimitConfigDto>> getActiveConfigurations();
    
    /**
     * Find best matching configuration for a request context
     */
    @GetMapping("/match")
    ApiResponse<RateLimitConfigDto> findMatchingConfig(
            @RequestParam String serviceName,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String environment
    );
}
