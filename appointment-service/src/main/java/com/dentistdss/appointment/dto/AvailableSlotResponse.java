package com.dentistdss.appointment.dto;

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
public class AvailableSlotResponse {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long dentistId;
    private String dentistName;
    private Long clinicId;
    private boolean available;
}
