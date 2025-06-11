package com.dentistdss.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.auth.model.UserApprovalRequest;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface UserApprovalRequestRepository extends JpaRepository<UserApprovalRequest, Integer> {
    
    @Query(nativeQuery = true, value = "SELECT * FROM user_approval_requests WHERE status = CAST(:status AS approval_status)")
    List<UserApprovalRequest> findByStatus(@Param("status") String status);
    
    List<UserApprovalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query(nativeQuery = true, value = "SELECT * FROM user_approval_requests WHERE user_id = :userId AND status = CAST(:status AS approval_status)")
    Optional<UserApprovalRequest> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query(nativeQuery = true, value = "SELECT * FROM user_approval_requests WHERE clinic_id = :clinicId AND status = CAST(:status AS approval_status) ORDER BY created_at DESC")
    List<UserApprovalRequest> findByClinicIdAndStatusOrderByCreatedAtDesc(@Param("clinicId") Long clinicId, @Param("status") String status);
    
    List<UserApprovalRequest> findByReviewedByOrderByReviewedAtDesc(Long reviewedBy);
    
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE user_approval_requests SET " +
            "status = CAST(:status AS approval_status), " +
            "review_notes = :reviewNotes, " +
            "reviewed_by = :reviewedBy, " +
            "reviewed_at = NOW(), " +
            "updated_at = NOW() " +
            "WHERE id = :id")
    void updateApprovalStatus(
            @Param("id") Integer id,
            @Param("status") String status,
            @Param("reviewNotes") String reviewNotes,
            @Param("reviewedBy") Long reviewedBy);
} 