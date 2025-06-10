package press.mizhifei.dentist.clinicalrecords.dto;

import jakarta.validation.constraints.NotNull;
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
public class ServiceVisitRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;
    
    private Long appointmentId;
    
    @NotNull(message = "Visit type is required")
    private String visitType; // ROUTINE, EMERGENCY, CONSULTATION, FOLLOW_UP
    
    @NotNull(message = "Visit date is required")
    private LocalDateTime visitDate;
    
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime;
    
    private String notes;
}
