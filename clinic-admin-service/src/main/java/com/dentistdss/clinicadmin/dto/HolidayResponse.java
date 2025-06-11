package com.dentistdss.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.clinicadmin.model.Holiday;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for holiday operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private Long id;
    private Long clinicId;
    private String clinicName;
    private String name;
    private LocalDate holidayDate;
    private String description;
    private Holiday.HolidayType type;
    private String typeDisplayName;

    private Boolean isFullDayClosure;
    private LocalTime specialOpeningTime;
    private LocalTime specialClosingTime;
    private Boolean isRecurring;
    private String emergencyContact;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private String displayInfo; // Human-readable holiday information
    private Boolean hasSpecialHours;
    private Boolean isUpcoming; // Whether the holiday is in the future
    private Long daysUntilHoliday; // Days until the holiday (if upcoming)
}
