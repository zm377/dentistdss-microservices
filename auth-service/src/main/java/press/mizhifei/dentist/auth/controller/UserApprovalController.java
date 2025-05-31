package press.mizhifei.dentist.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.auth.annotation.RequireRoles;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.ApprovalRequestResponse;
import press.mizhifei.dentist.auth.dto.ReviewApprovalRequest;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;
import press.mizhifei.dentist.auth.security.UserPrincipal;
import press.mizhifei.dentist.auth.service.UserApprovalService;

import java.util.Arrays;
import java.util.List;

import static press.mizhifei.dentist.auth.util.HttpUtil.getJwtFromRequest;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/auth/approval")
@RequiredArgsConstructor
public class UserApprovalController {

    private final UserApprovalService userApprovalService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createApprovalRequest(
            @RequestParam(required = false) String requestReason) {
        Long userId = getCurrentUserId();
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .createApprovalRequest(userId, requestReason);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{requestId}/review")
    @RequireRoles({Role.SYSTEM_ADMIN, Role.CLINIC_ADMIN})
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> reviewApprovalRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody ReviewApprovalRequest reviewRequest) {
        Long reviewerId = getCurrentUserId();
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .reviewApprovalRequest(requestId, reviewRequest, reviewerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    @RequireRoles({Role.SYSTEM_ADMIN, Role.CLINIC_ADMIN})
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getPendingApprovalRequests(HttpServletRequest request) {
        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);
            if (!StringUtils.hasText(jwt)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication token is required"));
            }

            // Extract user information from token
            String userEmail = jwtTokenProvider.getEmailFromJWT(jwt);
            String rolesString = jwtTokenProvider.getRolesFromJWT(jwt);

            if (!StringUtils.hasText(userEmail) || !StringUtils.hasText(rolesString)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            // Parse roles from comma-separated string
            List<String> userRoles = Arrays.asList(rolesString.split(","));

            // Check if user has required roles (SYSTEM_ADMIN or CLINIC_ADMIN)
            boolean hasSystemAdmin = userRoles.contains(Role.SYSTEM_ADMIN.name());
            boolean hasClinicAdmin = userRoles.contains(Role.CLINIC_ADMIN.name());

            if (!hasSystemAdmin && !hasClinicAdmin) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You are not a legal administrator"));
            }

            // Get filtered approval requests based on role
            List<ApprovalRequestResponse> requests;
            if (hasSystemAdmin) {
                // SYSTEM_ADMIN can see all pending requests
                requests = userApprovalService.getPendingApprovalRequests();
            } else {
                // CLINIC_ADMIN can only see requests from their clinic
                requests = userApprovalService.getPendingApprovalRequests(userEmail);
            }

            return ResponseEntity.ok(ApiResponse.success(requests));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error processing request: " + e.getMessage()));
        }
    }
    
    @GetMapping("/clinic/{clinicId}/pending")
    @RequireRoles(Role.CLINIC_ADMIN)
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getClinicPendingApprovals(
            @PathVariable Long clinicId) {
        List<ApprovalRequestResponse> requests = userApprovalService.getClinicPendingApprovals(clinicId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getUserApprovalHistory(
            @PathVariable Long userId) {
        List<ApprovalRequestResponse> requests = userApprovalService.getUserApprovalHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getMyApprovalHistory() {
        Long userId = getCurrentUserId();
        List<ApprovalRequestResponse> requests = userApprovalService.getUserApprovalHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    @GetMapping("/reviewed-by-me")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getReviewedByMe() {
        Long reviewerId = getCurrentUserId();
        List<ApprovalRequestResponse> requests = userApprovalService.getReviewedByUser(reviewerId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        // For testing, return a default user ID
        return 1L;
    }
}