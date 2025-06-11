package press.mizhifei.dentist.clinicadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.clinicadmin.model.Holiday;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for holiday operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequest {

    @NotBlank(message = "Holiday name is required")
    @Size(max = 255, message = "Holiday name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Builder.Default
    private Holiday.HolidayType type = Holiday.HolidayType.CLINIC_SPECIFIC;

    // Whether the clinic is completely closed or has special hours
    @Builder.Default
    private Boolean isFullDayClosure = true;

    // If not full day closure, specify special hours
    private LocalTime specialOpeningTime;
    private LocalTime specialClosingTime;

    // Whether this is a recurring annual holiday
    @Builder.Default
    private Boolean isRecurring = false;

    // Emergency contact information for this holiday
    @Size(max = 500, message = "Emergency contact must not exceed 500 characters")
    private String emergencyContact;
}
