package com.dentistdss.userprofile.client;

import com.dentistdss.userprofile.dto.ClinicApprovalUpdateRequest;
import com.dentistdss.userprofile.dto.UserApprovalUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.dentistdss.userprofile.dto.ApiResponse;

/**
 * Feign client for auth service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

    @PutMapping("/user/{userId}/approval")
    ResponseEntity<ApiResponse<String>> updateUserApprovalStatus(
            @PathVariable("userId") Long userId,
            @RequestBody UserApprovalUpdateRequest request);

    @PutMapping("/clinic/{clinicId}/approval")
    ResponseEntity<ApiResponse<String>> updateClinicApprovalStatus(
            @PathVariable("clinicId") Long clinicId,
            @RequestBody ClinicApprovalUpdateRequest request);
}
