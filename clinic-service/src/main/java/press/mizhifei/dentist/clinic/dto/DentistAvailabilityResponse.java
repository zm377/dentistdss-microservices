package press.mizhifei.dentist.clinic.dto;

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
public class DentistAvailabilityResponse {
    private Integer id;
    private Long dentistId;
    private Long clinicId;
    private Integer dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isRecurring;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private Boolean isActive;
} 