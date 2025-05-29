package press.mizhifei.dentist.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.ApprovalRequestResponse;
import press.mizhifei.dentist.auth.dto.ReviewApprovalRequest;
import press.mizhifei.dentist.auth.security.UserPrincipal;
import press.mizhifei.dentist.auth.service.UserApprovalService;

import java.util.List;

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
    
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createApprovalRequest(
            @RequestParam(required = false) String requestReason) {
        Long userId = getCurrentUserId();
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .createApprovalRequest(userId, requestReason);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{requestId}/review")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> reviewApprovalRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody ReviewApprovalRequest reviewRequest) {
        Long reviewerId = getCurrentUserId();
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .reviewApprovalRequest(requestId, reviewRequest, reviewerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getPendingApprovalRequests() {
        List<ApprovalRequestResponse> requests = userApprovalService.getPendingApprovalRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    @GetMapping("/clinic/{clinicId}/pending")
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