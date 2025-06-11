package com.dentistdss.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
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
public class ApprovalRequestResponse {
    private Integer id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String requestedRole;
    private Long clinicId;
    private String clinicName;
    private String status;
    private String requestReason;
    private String[] supportingDocuments;
    private Long reviewedBy;
    private String reviewerName;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
} 