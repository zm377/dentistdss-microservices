package com.dentistdss.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
public class ServiceResponse {
    private Integer id;
    private Long clinicId;
    private String clinicName;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;
} 