package press.mizhifei.dentist.clinic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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
public class DentistAvailabilityRequest {
    
    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;
    
    @NotNull(message = "Day of week is required")
    private Integer dayOfWeek; // 0=Sunday, 1=Monday, ..., 6=Saturday
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    private Boolean isRecurring = true;
    
    private LocalDate effectiveFrom;
    
    private LocalDate effectiveUntil;
} 