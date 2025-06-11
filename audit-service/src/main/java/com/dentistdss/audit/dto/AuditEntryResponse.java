package com.dentistdss.audit.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

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
public class AuditEntryResponse {
    private String id;
    private String actor;
    private String action;
    private String target;
    private LocalDateTime timestamp;
    private Map<String, Object> context;
} 