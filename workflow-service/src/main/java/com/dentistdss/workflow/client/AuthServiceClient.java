package com.dentistdss.workflow.client;

import com.dentistdss.workflow.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for auth-service
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {
    
    @GetMapping("/user/{userId}")
    ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetails(@PathVariable("userId") Long userId);
    
    @PutMapping("/user/{userId}/approval")
    ResponseEntity<ApiResponse<String>> updateUserApprovalStatus(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, Object> request);
    
    @PutMapping("/clinic/{clinicId}/approval")
    ResponseEntity<ApiResponse<String>> updateClinicApprovalStatus(
            @PathVariable("clinicId") Long clinicId,
            @RequestBody Map<String, Object> request);
}
