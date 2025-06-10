package press.mizhifei.dentist.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.ApprovalRequestResponse;
import press.mizhifei.dentist.auth.dto.ReviewApprovalRequest;
import press.mizhifei.dentist.auth.model.Clinic;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.model.UserApprovalRequest;
import press.mizhifei.dentist.auth.repository.ClinicRepository;
import press.mizhifei.dentist.auth.repository.UserApprovalRequestRepository;
import press.mizhifei.dentist.auth.repository.UserRepository;
import press.mizhifei.dentist.auth.client.NotificationServiceClient;
import press.mizhifei.dentist.auth.dto.NotificationEmailRequest;

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

    private final UserApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final NotificationServiceClient notificationServiceClient;

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

        // Use the native query method with explicit casting
        UserApprovalRequest saved = approvalRequestRepository.saveWithCasting(
                userId,
                requestedRole.toString(),
                user.getClinicId(),
                User.ApprovalStatus.PENDING.toString(),
                requestReason
        );

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

        // Update approval request using native query with casting
        String newStatus = reviewRequest.getApproved() ? 
                User.ApprovalStatus.APPROVED.toString() : 
                User.ApprovalStatus.REJECTED.toString();
                
        UserApprovalRequest updatedRequest = approvalRequestRepository.updateWithCasting(
                requestId,
                newStatus,
                reviewRequest.getReviewNotes(),
                reviewedBy
        );

        // Update user if approved
        if (reviewRequest.getApproved()) {
            user.setApprovalStatus(User.ApprovalStatus.APPROVED);
            user.setApprovedBy(reviewedBy.toString());
            user.setApprovalDate(LocalDateTime.now());
            user.setEnabled(true);

            // For staff roles, ensure they have the correct role
            if (approvalRequest.getRequestedRole() != Role.PATIENT) {
                user.getRoles().add(approvalRequest.getRequestedRole());
            }
        } else {
            user.setApprovalStatus(User.ApprovalStatus.REJECTED);
            user.setApprovalRejectionReason(reviewRequest.getReviewNotes());
        }


        // if user is clinic admin, approve and enable clinic info
        if (user.getRoles().contains(Role.CLINIC_ADMIN) && reviewRequest.getApproved()) {
            Clinic clinic = clinicRepository.findById(approvalRequest.getClinicId())
                    .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
            clinic.setApproved(true);
            clinic.setApprovalBy(reviewedBy);
            clinic.setApprovalDate(LocalDateTime.now());
            clinic.setEnabled(true);
            clinicRepository.save(clinic);
            userRepository.save(user);
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

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getReviewedByUser(Long reviewerId) {
        List<UserApprovalRequest> requests = approvalRequestRepository
                .findByReviewedByOrderByReviewedAtDesc(reviewerId);

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

    private void sendApprovalNotification(User user, Role requestedRole) {
        // Determine who should be notified
        List<User> approvers;

        if (requestedRole == Role.CLINIC_ADMIN || user.getClinicId() == null) {
            // System admin approves clinic admins or users without clinic
            approvers = userRepository.findByRoles(Role.SYSTEM_ADMIN);
        } else {
            // Clinic admin approves staff within their clinic
            approvers = userRepository.findByClinicIdAndRoles(user.getClinicId(), Role.CLINIC_ADMIN);
        }

        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("user_name", user.getFirstName() + " " + user.getLastName());
        templateVariables.put("role", requestedRole.toString());

        approvers.forEach(approver -> {
            notificationServiceClient.sendNotificationEmail(
                    new NotificationEmailRequest(approver.getEmail(), "user_approval_request", templateVariables));
        });
    }

    private void sendApprovalResultNotification(User user, boolean approved, String reviewNotes) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("user_name", user.getFirstName() + " " + user.getLastName());
        templateVariables.put("role", user.getRoles().iterator().next().toString());
        templateVariables.put("status", approved ? "approved" : "rejected");
        templateVariables.put("reason", reviewNotes != null ? reviewNotes : "");

        notificationServiceClient.sendNotificationEmail(
                new NotificationEmailRequest(user.getEmail(), "user_approval_result", templateVariables));
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

        // Add clinic name if applicable
        if (request.getClinicId() != null) {
            clinicRepository.findById(request.getClinicId())
                    .ifPresent(clinic -> response.setClinicName(clinic.getName()));
        }

        // Add reviewer name if applicable
        if (request.getReviewedBy() != null) {
            userRepository.findById(request.getReviewedBy()).ifPresent(
                    reviewer -> response.setReviewerName(reviewer.getFirstName() + " " + reviewer.getLastName()));
        }

        return response;
    }
}