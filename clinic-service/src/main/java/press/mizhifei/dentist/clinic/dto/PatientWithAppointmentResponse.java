package press.mizhifei.dentist.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for patient information with appointment details
 * Used for the clinic patients list endpoint
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
public class PatientWithAppointmentResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private String healthHistory;
    
    /**
     * Date of most recent completed appointment (ISO date format)
     * Null if no previous appointments
     */
    private LocalDate lastVisit;
    
    /**
     * Date and time of next scheduled appointment (ISO datetime format)
     * Null if no upcoming appointments
     */
    private LocalDateTime nextAppointment;
}
