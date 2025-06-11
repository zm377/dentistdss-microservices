package com.dentistdss.clinicalrecords.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
public class TreatmentPlanResponse {
    private Integer id;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private Long clinicId;
    private String clinicName;
    private String planName;
    private String description;
    private BigDecimal totalCost;
    private BigDecimal insuranceCoverage;
    private BigDecimal patientCost;
    private String status;
    private Integer version;
    private Integer parentPlanId;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private List<TreatmentPlanItemResponse> items;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreatmentPlanItemResponse {
        private Integer id;
        private Integer serviceId;
        private String serviceName;
        private String toothNumber;
        private String description;
        private BigDecimal cost;
        private String status;
        private Integer sequenceOrder;
        private String notes;
    }
}
