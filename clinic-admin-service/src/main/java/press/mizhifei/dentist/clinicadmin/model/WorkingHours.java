package press.mizhifei.dentist.clinicadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Working Hours entity for clinic schedule management
 * 
 * Supports both regular weekly schedules and special date-specific hours
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
@Table(name = "working_hours", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"clinic_id", "day_of_week", "specific_date"})
       })
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    @NotNull(message = "Clinic is required")
    private Clinic clinic;

    // For regular weekly schedule
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    // For special date-specific hours (overrides regular schedule)
    private LocalDate specificDate;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    // Break times (optional)
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    // Whether the clinic is closed on this day/date
    @Builder.Default
    private Boolean isClosed = false;

    // Whether this is an emergency hours schedule
    @Builder.Default
    private Boolean isEmergencyHours = false;

    // Notes for special instructions
    private String notes;

    // Audit fields
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void auditAndValidate() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        // Trim notes
        if (notes != null) {
            notes = notes.trim();
        }

        // Validation: either dayOfWeek or specificDate must be set, but not both
        if (dayOfWeek == null && specificDate == null) {
            throw new IllegalStateException("Either dayOfWeek or specificDate must be specified");
        }
        if (dayOfWeek != null && specificDate != null) {
            throw new IllegalStateException("Cannot specify both dayOfWeek and specificDate");
        }

        // Validation: opening time must be before closing time
        if (!isClosed && openingTime != null && closingTime != null) {
            if (openingTime.isAfter(closingTime) || openingTime.equals(closingTime)) {
                throw new IllegalStateException("Opening time must be before closing time");
            }
        }

        // Validation: break times must be within working hours
        if (breakStartTime != null && breakEndTime != null && !isClosed) {
            if (breakStartTime.isAfter(breakEndTime) || breakStartTime.equals(breakEndTime)) {
                throw new IllegalStateException("Break start time must be before break end time");
            }
            if (openingTime != null && closingTime != null) {
                if (breakStartTime.isBefore(openingTime) || breakEndTime.isAfter(closingTime)) {
                    throw new IllegalStateException("Break times must be within working hours");
                }
            }
        }
    }

    /**
     * Check if the working hours are for a regular weekly schedule
     */
    public boolean isRegularSchedule() {
        return dayOfWeek != null && specificDate == null;
    }

    /**
     * Check if the working hours are for a specific date
     */
    public boolean isSpecificDate() {
        return specificDate != null && dayOfWeek == null;
    }

    /**
     * Get the effective date for this working hours entry
     */
    public String getEffectiveScheduleDescription() {
        if (isRegularSchedule()) {
            return "Every " + dayOfWeek.toString();
        } else if (isSpecificDate()) {
            return "Specific date: " + specificDate.toString();
        }
        return "Unknown schedule type";
    }

    /**
     * Check if the clinic has break time on this schedule
     */
    public boolean hasBreakTime() {
        return breakStartTime != null && breakEndTime != null;
    }
}
