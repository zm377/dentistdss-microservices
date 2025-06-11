package com.dentistdss.audit.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_entries")
public class AuditEntry {
    @Id
    private String id;

    private String actor;       // user or service that triggered
    private String action;      // e.g. CREATE_PATIENT
    private String target;      // resource id or description

    private LocalDateTime timestamp;

    private Map<String, Object> context; // additional metadata
} 