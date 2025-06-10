package press.mizhifei.dentist.userprofile.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.userprofile.dto.ApiResponse;
import press.mizhifei.dentist.userprofile.dto.ApprovalRequestResponse;
import press.mizhifei.dentist.userprofile.dto.ReviewApprovalRequest;
import press.mizhifei.dentist.userprofile.service.UserApprovalService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/user/approval")
@RequiredArgsConstructor
public class UserApprovalController {

    private final UserApprovalService userApprovalService;
    
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createApprovalRequest(
            @RequestParam Long userId,
            @RequestParam(required = false) String requestReason) {
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .createApprovalRequest(userId, requestReason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/review")
    public ResponseEntity<ApiResponse<ApprovalRequestResponse>> reviewApprovalRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody ReviewApprovalRequest reviewRequest,
            @RequestParam Long reviewedBy) {
        ApiResponse<ApprovalRequestResponse> response = userApprovalService
                .reviewApprovalRequest(requestId, reviewRequest, reviewedBy);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> getPendingApprovalRequests(
            @RequestParam(required = false) String userEmail) {
        List<ApprovalRequestResponse> requests;
        if (userEmail != null) {
            requests = userApprovalService.getPendingApprovalRequests(userEmail);
        } else {
            requests = userApprovalService.getPendingApprovalRequests();
        }
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
}
