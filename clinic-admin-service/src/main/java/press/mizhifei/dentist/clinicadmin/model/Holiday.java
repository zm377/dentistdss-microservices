package press.mizhifei.dentist.clinicadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Holiday entity for clinic holiday management
 * 
 * Supports both national holidays and clinic-specific holidays
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
@Table(name = "holidays",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"clinic_id", "holiday_date"})
       })
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    @NotNull(message = "Clinic is required")
    private Clinic clinic;

    @NotBlank(message = "Holiday name is required")
    @Size(max = 255, message = "Holiday name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HolidayType type = HolidayType.CLINIC_SPECIFIC;

    // Whether the clinic is completely closed or has special hours
    @Builder.Default
    private Boolean isFullDayClosure = true;

    // If not full day closure, specify special hours
    private java.time.LocalTime specialOpeningTime;
    private java.time.LocalTime specialClosingTime;

    // Whether this is a recurring annual holiday
    @Builder.Default
    private Boolean isRecurring = false;

    // Emergency contact information for this holiday
    @Size(max = 500, message = "Emergency contact must not exceed 500 characters")
    private String emergencyContact;

    // Audit fields
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Holiday types for categorization
     */
    public enum HolidayType {
        NATIONAL_HOLIDAY("National Holiday"),
        CLINIC_SPECIFIC("Clinic Specific"),
        EMERGENCY_CLOSURE("Emergency Closure"),
        STAFF_TRAINING("Staff Training"),
        MAINTENANCE("Maintenance");

        private final String displayName;

        HolidayType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    @PreUpdate
    private void auditAndValidate() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        // Trim string fields
        if (name != null) name = name.trim();
        if (description != null) description = description.trim();
        if (emergencyContact != null) emergencyContact = emergencyContact.trim();

        // Validation: if not full day closure, special hours must be provided
        if (!isFullDayClosure) {
            if (specialOpeningTime == null || specialClosingTime == null) {
                throw new IllegalStateException("Special opening and closing times must be provided when not a full day closure");
            }
            if (specialOpeningTime.isAfter(specialClosingTime) || specialOpeningTime.equals(specialClosingTime)) {
                throw new IllegalStateException("Special opening time must be before special closing time");
            }
        }

        // Validation: holiday date cannot be in the past (except for emergency closures)
        if (type != HolidayType.EMERGENCY_CLOSURE && holidayDate != null && holidayDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Holiday date cannot be in the past (except for emergency closures)");
        }
    }

    /**
     * Check if this holiday affects the given date
     */
    public boolean affectsDate(LocalDate date) {
        if (isRecurring) {
            // For recurring holidays, check month and day
            return holidayDate.getMonth() == date.getMonth() && 
                   holidayDate.getDayOfMonth() == date.getDayOfMonth();
        } else {
            // For non-recurring holidays, exact date match
            return holidayDate.equals(date);
        }
    }

    /**
     * Get display information for this holiday
     */
    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();
        info.append(name);
        if (isRecurring) {
            info.append(" (Annual)");
        }
        info.append(" - ").append(holidayDate);
        if (!isFullDayClosure) {
            info.append(" (Special hours: ")
                .append(specialOpeningTime)
                .append(" - ")
                .append(specialClosingTime)
                .append(")");
        } else {
            info.append(" (Closed)");
        }
        return info.toString();
    }

    /**
     * Check if the clinic has special hours on this holiday
     */
    public boolean hasSpecialHours() {
        return !isFullDayClosure && specialOpeningTime != null && specialClosingTime != null;
    }
}
