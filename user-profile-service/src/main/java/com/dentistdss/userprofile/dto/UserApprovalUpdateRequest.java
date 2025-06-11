package com.dentistdss.userprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.userprofile.model.Role;

import java.time.LocalDateTime;

/**
 * Request DTO for updating user approval status
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserApprovalUpdateRequest {
    private String approvalStatus; // APPROVED, REJECTED
    private String approvedBy;
    private LocalDateTime approvalDate;
    private Boolean enabled;
    private Role requestedRole;
    private String rejectionReason;
}
