package com.dentistdss.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for working hours operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursResponse {

    private Long id;
    private Long clinicId;
    private String clinicName;

    // For regular weekly schedule
    private DayOfWeek dayOfWeek;

    // For special date-specific hours
    private LocalDate specificDate;

    private LocalTime openingTime;
    private LocalTime closingTime;

    // Break times
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    private Boolean isClosed;
    private Boolean isEmergencyHours;
    private String notes;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private String scheduleType; // "REGULAR" or "SPECIFIC_DATE"
    private String displaySchedule; // Human-readable schedule description
    private Boolean hasBreakTime;
}
