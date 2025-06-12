package com.dentistdss.workflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for step approval/rejection
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepApprovalRequest {
    
    @NotNull(message = "Approval decision is required")
    private Boolean approved;
    
    private String approvalNotes;
    
    private Map<String, Object> outputData;
}
