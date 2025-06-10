package press.mizhifei.dentist.userprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.userprofile.dto.ApiResponse;
import press.mizhifei.dentist.userprofile.dto.ApprovalRequestResponse;
import press.mizhifei.dentist.userprofile.dto.ReviewApprovalRequest;
import press.mizhifei.dentist.userprofile.model.Role;
import press.mizhifei.dentist.userprofile.model.User;
import press.mizhifei.dentist.userprofile.model.UserApprovalRequest;
import press.mizhifei.dentist.userprofile.repository.UserApprovalRequestRepository;
import press.mizhifei.dentist.userprofile.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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

        userRepository.save(user);

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
        // Logic to determine what role the user is requesting
        // This is a simplified version - you may want to make this more sophisticated
        if (user.getRoles().contains(Role.PATIENT)) {
            return Role.DENTIST; // Patient requesting to become dentist
        }
        return Role.CLINIC_ADMIN; // Default to clinic admin
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
