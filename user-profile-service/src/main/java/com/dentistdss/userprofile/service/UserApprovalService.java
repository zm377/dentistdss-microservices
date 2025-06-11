package com.dentistdss.userprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.userprofile.dto.ApiResponse;
import com.dentistdss.userprofile.dto.ApprovalRequestResponse;
import com.dentistdss.userprofile.dto.ReviewApprovalRequest;
import com.dentistdss.userprofile.dto.NotificationEmailRequest;
import com.dentistdss.userprofile.dto.UserApprovalUpdateRequest;
import com.dentistdss.userprofile.dto.ClinicApprovalUpdateRequest;
import com.dentistdss.userprofile.model.Role;
import com.dentistdss.userprofile.model.User;
import com.dentistdss.userprofile.model.UserApprovalRequest;
import com.dentistdss.userprofile.repository.UserApprovalRequestRepository;
import com.dentistdss.userprofile.repository.UserRepository;
import com.dentistdss.userprofile.client.NotificationServiceClient;
import com.dentistdss.userprofile.client.AuthServiceClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApprovalService {

    private final UserRepository userRepository;
    private final UserApprovalRequestRepository approvalRequestRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public ApiResponse<ApprovalRequestResponse> createApprovalRequest(Long userId, String requestReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if there's already a pending request
        Optional<UserApprovalRequest> existingRequest = approvalRequestRepository
                .findByUserIdAndStatus(userId, User.ApprovalStatus.PENDING.toString());

        if (existingRequest.isPresent()) {
            return ApiResponse.error("Approval request already exists. Please wait for the review.");
        }

        // Determine requested role based on user's current roles
        Role requestedRole = determineRequestedRole(user);

        // Create and save the approval request using JPA
        UserApprovalRequest approvalRequest = UserApprovalRequest.builder()
                .userId(userId)
                .requestedRole(requestedRole)
                .clinicId(user.getClinicId())
                .status(User.ApprovalStatus.PENDING)
                .requestReason(requestReason)
                .build();

        UserApprovalRequest saved = approvalRequestRepository.save(approvalRequest);

        // Send notification to approvers
        sendApprovalNotification(user, requestedRole);

        log.info("Created approval request {} for user {} requesting role {}",
                saved.getId(), userId, requestedRole);

        return ApiResponse.success(toResponse(saved));
    }

    @Transactional
    public ApiResponse<ApprovalRequestResponse> reviewApprovalRequest(Integer requestId,
            ReviewApprovalRequest reviewRequest,
            Long reviewedBy) {
        UserApprovalRequest approvalRequest = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approvalRequest.getStatus() != User.ApprovalStatus.PENDING) {
            return ApiResponse.error("No pending approval request found");
        }

        User user = userRepository.findById(approvalRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update approval request using the new method
        User.ApprovalStatus newStatus = reviewRequest.getApproved() ?
                User.ApprovalStatus.APPROVED :
                User.ApprovalStatus.REJECTED;

        approvalRequest.setStatus(newStatus);
        approvalRequest.setReviewedBy(reviewedBy);
        approvalRequest.setReviewNotes(reviewRequest.getReviewNotes());
        approvalRequest.setReviewedAt(LocalDateTime.now());

        UserApprovalRequest updatedRequest = approvalRequestRepository.save(approvalRequest);

        // Update user approval status via auth-service
        updateUserApprovalStatus(user, approvalRequest, reviewRequest, reviewedBy);

        // If user is clinic admin, approve and enable clinic info via auth-service
        if (user.getRoles().contains(Role.CLINIC_ADMIN) && reviewRequest.getApproved()) {
            updateClinicApprovalStatus(approvalRequest.getClinicId(), reviewedBy);
        }

        // Send notification to user
        sendApprovalResultNotification(user, reviewRequest.getApproved(), reviewRequest.getReviewNotes());

        log.info("Reviewed approval request {} for user {} - {}",
                requestId, approvalRequest.getUserId(),
                reviewRequest.getApproved() ? "APPROVED" : "REJECTED");

        return ApiResponse.success(toResponse(updatedRequest));
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getPendingApprovalRequests() {
        List<UserApprovalRequest> pendingRequests = approvalRequestRepository
                .findByStatus(User.ApprovalStatus.PENDING.toString());

        return pendingRequests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getPendingApprovalRequests(String userEmail) {
        // Get user information to determine clinic filtering
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        List<UserApprovalRequest> pendingRequests;

        // Filter by clinic if user has clinic_id and is CLINIC_ADMIN
        if (user.getClinicId() != null && user.getRoles().contains(Role.CLINIC_ADMIN)) {
            pendingRequests = approvalRequestRepository
                    .findByClinicIdAndStatusOrderByCreatedAtDesc(user.getClinicId(), User.ApprovalStatus.PENDING.toString());
        } else {
            // If no clinic_id, return all pending requests (for SYSTEM_ADMIN)
            pendingRequests = approvalRequestRepository
                    .findByStatus(User.ApprovalStatus.PENDING.toString());
        }

        return pendingRequests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getClinicPendingApprovals(Long clinicId) {
        List<UserApprovalRequest> pendingRequests = approvalRequestRepository
                .findByClinicIdAndStatusOrderByCreatedAtDesc(clinicId, User.ApprovalStatus.PENDING.toString());

        return pendingRequests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getUserApprovalHistory(Long userId) {
        List<UserApprovalRequest> requests = approvalRequestRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        return requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Role determineRequestedRole(User user) {
        if (user.getRoles().contains(Role.DENTIST)) {
            return Role.DENTIST;
        } else if (user.getRoles().contains(Role.RECEPTIONIST)) {
            return Role.RECEPTIONIST;
        } else if (user.getRoles().contains(Role.CLINIC_ADMIN)) {
            return Role.CLINIC_ADMIN;
        }
        return Role.PATIENT;
    }

    private void updateUserApprovalStatus(User user, UserApprovalRequest approvalRequest,
                                        ReviewApprovalRequest reviewRequest, Long reviewedBy) {
        UserApprovalUpdateRequest updateRequest = UserApprovalUpdateRequest.builder()
                .approvalStatus(reviewRequest.getApproved() ? "APPROVED" : "REJECTED")
                .approvedBy(reviewedBy.toString())
                .approvalDate(reviewRequest.getApproved() ? LocalDateTime.now() : null)
                .enabled(reviewRequest.getApproved())
                .requestedRole(reviewRequest.getApproved() ? approvalRequest.getRequestedRole() : null)
                .rejectionReason(reviewRequest.getApproved() ? null : reviewRequest.getReviewNotes())
                .build();

        try {
            authServiceClient.updateUserApprovalStatus(user.getId(), updateRequest);
        } catch (Exception e) {
            log.error("Failed to update user approval status for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private void updateClinicApprovalStatus(Long clinicId, Long reviewedBy) {
        ClinicApprovalUpdateRequest updateRequest = ClinicApprovalUpdateRequest.builder()
                .approved(true)
                .approvalBy(reviewedBy)
                .approvalDate(LocalDateTime.now())
                .enabled(true)
                .build();

        try {
            authServiceClient.updateClinicApprovalStatus(clinicId, updateRequest);
        } catch (Exception e) {
            log.error("Failed to update clinic approval status for clinic {}: {}", clinicId, e.getMessage());
        }
    }

    private void sendApprovalNotification(User user, Role requestedRole) {
        // Determine who should be notified
        List<User> approvers;

        if (requestedRole == Role.CLINIC_ADMIN || user.getClinicId() == null) {
            // System admin approves clinic admins or users without clinic
            approvers = userRepository.findByRole(Role.SYSTEM_ADMIN);
        } else {
            // Clinic admin approves staff within their clinic
            approvers = userRepository.findByClinicIdAndRoles(user.getClinicId(), Role.CLINIC_ADMIN);
        }

        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("user_name", user.getFirstName() + " " + user.getLastName());
        templateVariables.put("role", requestedRole.toString());

        approvers.forEach(approver -> {
            try {
                notificationServiceClient.sendNotificationEmail(
                        new NotificationEmailRequest(approver.getEmail(), "user_approval_request", templateVariables));
            } catch (Exception e) {
                log.error("Failed to send approval notification to {}: {}", approver.getEmail(), e.getMessage());
            }
        });
    }

    private void sendApprovalResultNotification(User user, boolean approved, String reviewNotes) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("user_name", user.getFirstName() + " " + user.getLastName());
        templateVariables.put("role", user.getRoles().iterator().next().toString());
        templateVariables.put("status", approved ? "approved" : "rejected");
        templateVariables.put("reason", reviewNotes != null ? reviewNotes : "");

        try {
            notificationServiceClient.sendNotificationEmail(
                    new NotificationEmailRequest(user.getEmail(), "user_approval_result", templateVariables));
        } catch (Exception e) {
            log.error("Failed to send approval result notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private ApprovalRequestResponse toResponse(UserApprovalRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);

        ApprovalRequestResponse response = ApprovalRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .userName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .requestedRole(request.getRequestedRole().toString())
                .clinicId(request.getClinicId())
                .status(request.getStatus().toString())
                .requestReason(request.getRequestReason())
                .supportingDocuments(request.getSupportingDocuments())
                .reviewedBy(request.getReviewedBy())
                .reviewNotes(request.getReviewNotes())
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .build();

        return response;
    }
}
