package com.dentistdss.clinicadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Clinic entity for comprehensive clinic administration
 * 
 * Supports full clinic management including working hours and holidays
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
@Table(name = "clinics")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Clinic name is required")
    @Size(max = 255, message = "Clinic name must not exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Time zone for the clinic (important for scheduling)
    @Builder.Default
    private String timeZone = ZoneId.systemDefault().getId();

    // Administrative fields
    @Builder.Default
    private Boolean enabled = false;

    @Builder.Default
    private Boolean approved = false;

    private Long approvalBy;

    // Audit fields
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkingHours> workingHours = new ArrayList<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Holiday> holidays = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void auditAndTrim() {
        // Trim all string fields
        if (name != null) name = name.trim();
        if (address != null) address = address.trim();
        if (city != null) city = city.trim();
        if (state != null) state = state.trim();
        if (zipCode != null) zipCode = zipCode.trim();
        if (country != null) country = country.trim();
        if (phoneNumber != null) phoneNumber = phoneNumber.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (website != null) website = website.trim();
        if (description != null) description = description.trim();
        if (timeZone != null) timeZone = timeZone.trim();

        // Update audit fields
        updatedAt = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Helper methods for working hours management
    public void addWorkingHours(WorkingHours workingHours) {
        this.workingHours.add(workingHours);
        workingHours.setClinic(this);
    }

    public void removeWorkingHours(WorkingHours workingHours) {
        this.workingHours.remove(workingHours);
        workingHours.setClinic(null);
    }

    // Helper methods for holiday management
    public void addHoliday(Holiday holiday) {
        this.holidays.add(holiday);
        holiday.setClinic(this);
    }

    public void removeHoliday(Holiday holiday) {
        this.holidays.remove(holiday);
        holiday.setClinic(null);
    }
}
