package com.dentistdss.clinicalrecords.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
public class TreatmentPlanRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;
    
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String planName;
    
    private String description;
    
    private BigDecimal totalCost;
    
    private BigDecimal insuranceCoverage;
    
    private BigDecimal patientCost;
    
    private Integer parentPlanId; // For creating plan revisions
    
    @Valid
    private List<TreatmentPlanItemRequest> items;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreatmentPlanItemRequest {
        
        private Integer serviceId;
        
        @Size(max = 10, message = "Tooth number must not exceed 10 characters")
        private String toothNumber;
        
        private String description;
        
        private BigDecimal cost;
        
        private Integer sequenceOrder;
        
        private String notes;
    }
}
