package com.dentistdss.clinicalrecords.dto;

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
public class ServiceVisitResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private Long clinicId;
    private String clinicName;
    private Long appointmentId;
    private String visitType;
    private LocalDateTime visitDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer durationMinutes;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
