package com.dentistdss.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.dentistdss.auth.dto.ApiResponse;
import com.dentistdss.auth.dto.ApprovalRequestResponse;
import com.dentistdss.auth.dto.UserResponse;

import java.util.List;

/**
 * Feign client for user-profile-service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "user-profile-service", path = "/user")
public interface UserProfileServiceClient {
    
    @GetMapping("/{id}/email")
    String getUserEmail(@PathVariable("id") Long userId);
    
    @GetMapping("/{id}/name")
    String getUserFullName(@PathVariable("id") Long userId);

    @GetMapping("/clinic/{clinicId}/dentists")
    List<UserResponse> getClinicDentists(@PathVariable("clinicId") Long clinicId);

    // User approval functionality
    @PostMapping("/approval/request")
    ResponseEntity<ApiResponse<ApprovalRequestResponse>> createApprovalRequest(
            @RequestParam Long userId,
            @RequestParam(required = false) String requestReason);
}
