package com.dentistdss.userprofile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medical_history")
public class MedicalHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "condition_name", nullable = false)
    private String conditionName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;
    
    @Column(name = "resolved_date")
    private LocalDate resolvedDate;
    
    @Column(name = "is_chronic")
    @Builder.Default
    private Boolean isChronic = false;
    
    @Column(columnDefinition = "TEXT")
    private String medications;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 