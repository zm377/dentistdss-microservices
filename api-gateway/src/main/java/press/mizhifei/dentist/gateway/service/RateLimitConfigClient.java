package press.mizhifei.dentist.gateway.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import press.mizhifei.dentist.gateway.dto.ApiResponse;
import press.mizhifei.dentist.gateway.dto.RateLimitConfigDto;

import java.util.List;

/**
 * Feign client for retrieving rate limit configurations from system service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@FeignClient(name = "system-service", path = "/system/rate-limit")
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
            @RequestParam String endpoint,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Long clinicId
    );
}
