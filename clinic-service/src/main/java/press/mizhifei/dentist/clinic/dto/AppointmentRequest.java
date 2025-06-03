package press.mizhifei.dentist.clinic.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AppointmentRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    @NotNull(message = "Created by is required")
    private Long createdBy;
    
    private Integer serviceId;
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Size(max = 500, message = "Reason for visit must not exceed 500 characters")
    private String reasonForVisit;
    
    @Size(max = 1000, message = "Symptoms description must not exceed 1000 characters")
    private String symptoms;
    
    private String urgencyLevel; // ROUTINE, MODERATE, URGENT, EMERGENCY
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
} 