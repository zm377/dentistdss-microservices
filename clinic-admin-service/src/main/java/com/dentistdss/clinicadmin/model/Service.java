package com.dentistdss.clinicadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service entity for clinic service management
 * 
 * Represents dental services offered by clinics
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "services")
public class Service {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "clinic_id", nullable = false)
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;
    
    @NotBlank(message = "Service name is required")
    @Size(max = 255, message = "Service name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Positive(message = "Duration must be positive")
    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 30;
    
    @Positive(message = "Price must be positive")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category; // Cleaning, Filling, Extraction, etc.
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    // Audit fields
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    @PreUpdate
    private void auditAndTrim() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        
        // Trim string fields
        if (name != null) name = name.trim();
        if (description != null) description = description.trim();
        if (category != null) category = category.trim();
    }
}
