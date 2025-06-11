package com.dentistdss.clinicadmin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for working hours operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursRequest {

    // For regular weekly schedule
    private DayOfWeek dayOfWeek;

    // For special date-specific hours
    private LocalDate specificDate;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    // Break times (optional)
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    // Whether the clinic is closed on this day/date
    @Builder.Default
    private Boolean isClosed = false;

    // Whether this is an emergency hours schedule
    @Builder.Default
    private Boolean isEmergencyHours = false;

    // Notes for special instructions
    private String notes;
}
