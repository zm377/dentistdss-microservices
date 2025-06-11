package com.dentistdss.clinicadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.clinicadmin.dto.WorkingHoursRequest;
import com.dentistdss.clinicadmin.dto.WorkingHoursResponse;
import com.dentistdss.clinicadmin.model.Clinic;
import com.dentistdss.clinicadmin.model.WorkingHours;
import com.dentistdss.clinicadmin.repository.ClinicRepository;
import com.dentistdss.clinicadmin.repository.WorkingHoursRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Working Hours Management Service
 * 
 * Handles clinic working hours operations with comprehensive
 * schedule management capabilities
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Get all working hours for a clinic
     */
    public List<WorkingHoursResponse> getClinicWorkingHours(Long clinicId) {
        log.debug("Fetching working hours for clinic ID: {}", clinicId);
        
        List<WorkingHours> workingHours = workingHoursRepository.findByClinicIdOrderByDayOfWeekAscSpecificDateAsc(clinicId);
        return workingHours.stream()
                .map(this::mapToWorkingHoursResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get regular weekly working hours for a clinic
     */
    public List<WorkingHoursResponse> getRegularWorkingHours(Long clinicId) {
        log.debug("Fetching regular working hours for clinic ID: {}", clinicId);
        
        List<WorkingHours> workingHours = workingHoursRepository.findByClinicIdAndDayOfWeekIsNotNullOrderByDayOfWeekAsc(clinicId);
        return workingHours.stream()
                .map(this::mapToWorkingHoursResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get specific date working hours for a clinic
     */
    public List<WorkingHoursResponse> getSpecificDateWorkingHours(Long clinicId) {
        log.debug("Fetching specific date working hours for clinic ID: {}", clinicId);
        
        List<WorkingHours> workingHours = workingHoursRepository.findByClinicIdAndSpecificDateIsNotNullOrderBySpecificDateAsc(clinicId);
        return workingHours.stream()
                .map(this::mapToWorkingHoursResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create or update working hours
     */
    @Transactional
    public WorkingHoursResponse createOrUpdateWorkingHours(Long clinicId, WorkingHoursRequest request) {
        log.info("Creating/updating working hours for clinic ID: {}", clinicId);
        
        // Verify clinic exists
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + clinicId));

        // Validate request
        validateWorkingHoursRequest(request);

        // Check if working hours already exist
        Optional<WorkingHours> existingWorkingHours = findExistingWorkingHours(clinicId, request);
        
        WorkingHours workingHours;
        if (existingWorkingHours.isPresent()) {
            // Update existing
            workingHours = existingWorkingHours.get();
            updateWorkingHoursFromRequest(workingHours, request);
            log.info("Updating existing working hours with ID: {}", workingHours.getId());
        } else {
            // Create new
            workingHours = createWorkingHoursFromRequest(clinic, request);
            log.info("Creating new working hours for clinic ID: {}", clinicId);
        }

        WorkingHours savedWorkingHours = workingHoursRepository.save(workingHours);
        return mapToWorkingHoursResponse(savedWorkingHours);
    }

    /**
     * Delete working hours
     */
    @Transactional
    public void deleteWorkingHours(Long clinicId, Long workingHoursId) {
        log.info("Deleting working hours with ID: {} for clinic ID: {}", workingHoursId, clinicId);
        
        WorkingHours workingHours = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new IllegalArgumentException("Working hours not found with ID: " + workingHoursId));

        if (!workingHours.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("Working hours does not belong to the specified clinic");
        }

        workingHoursRepository.delete(workingHours);
        log.info("Deleted working hours with ID: {}", workingHoursId);
    }

    /**
     * Check if clinic is open on a specific day
     */
    public boolean isClinicOpen(Long clinicId, DayOfWeek dayOfWeek) {
        return workingHoursRepository.isClinicOpenOnDayOfWeek(clinicId, dayOfWeek);
    }

    /**
     * Check if clinic is open on a specific date
     */
    public boolean isClinicOpen(Long clinicId, LocalDate date) {
        // First check for specific date override
        if (workingHoursRepository.isClinicOpenOnSpecificDate(clinicId, date)) {
            return true;
        }
        
        // Then check regular schedule
        return workingHoursRepository.isClinicOpenOnDayOfWeek(clinicId, date.getDayOfWeek());
    }

    /**
     * Get working hours for a specific day of week
     */
    public Optional<WorkingHoursResponse> getWorkingHoursForDay(Long clinicId, DayOfWeek dayOfWeek) {
        Optional<WorkingHours> workingHours = workingHoursRepository.findByClinicIdAndDayOfWeek(clinicId, dayOfWeek);
        return workingHours.map(this::mapToWorkingHoursResponse);
    }

    /**
     * Get working hours for a specific date
     */
    public Optional<WorkingHoursResponse> getWorkingHoursForDate(Long clinicId, LocalDate date) {
        Optional<WorkingHours> workingHours = workingHoursRepository.findByClinicIdAndSpecificDate(clinicId, date);
        return workingHours.map(this::mapToWorkingHoursResponse);
    }

    /**
     * Validate working hours request
     */
    private void validateWorkingHoursRequest(WorkingHoursRequest request) {
        // Either dayOfWeek or specificDate must be set, but not both
        if (request.getDayOfWeek() == null && request.getSpecificDate() == null) {
            throw new IllegalArgumentException("Either dayOfWeek or specificDate must be specified");
        }
        if (request.getDayOfWeek() != null && request.getSpecificDate() != null) {
            throw new IllegalArgumentException("Cannot specify both dayOfWeek and specificDate");
        }

        // If not closed, opening and closing times are required
        if (!request.getIsClosed()) {
            if (request.getOpeningTime() == null || request.getClosingTime() == null) {
                throw new IllegalArgumentException("Opening and closing times are required when not closed");
            }
            if (request.getOpeningTime().isAfter(request.getClosingTime()) || 
                request.getOpeningTime().equals(request.getClosingTime())) {
                throw new IllegalArgumentException("Opening time must be before closing time");
            }
        }

        // Validate break times if provided
        if (request.getBreakStartTime() != null && request.getBreakEndTime() != null) {
            if (request.getBreakStartTime().isAfter(request.getBreakEndTime()) || 
                request.getBreakStartTime().equals(request.getBreakEndTime())) {
                throw new IllegalArgumentException("Break start time must be before break end time");
            }
            
            if (!request.getIsClosed() && request.getOpeningTime() != null && request.getClosingTime() != null) {
                if (request.getBreakStartTime().isBefore(request.getOpeningTime()) || 
                    request.getBreakEndTime().isAfter(request.getClosingTime())) {
                    throw new IllegalArgumentException("Break times must be within working hours");
                }
            }
        }
    }

    /**
     * Find existing working hours for the same day/date
     */
    private Optional<WorkingHours> findExistingWorkingHours(Long clinicId, WorkingHoursRequest request) {
        if (request.getDayOfWeek() != null) {
            return workingHoursRepository.findByClinicIdAndDayOfWeek(clinicId, request.getDayOfWeek());
        } else if (request.getSpecificDate() != null) {
            return workingHoursRepository.findByClinicIdAndSpecificDate(clinicId, request.getSpecificDate());
        }
        return Optional.empty();
    }

    /**
     * Create new working hours from request
     */
    private WorkingHours createWorkingHoursFromRequest(Clinic clinic, WorkingHoursRequest request) {
        return WorkingHours.builder()
                .clinic(clinic)
                .dayOfWeek(request.getDayOfWeek())
                .specificDate(request.getSpecificDate())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .isClosed(request.getIsClosed())
                .isEmergencyHours(request.getIsEmergencyHours())
                .notes(request.getNotes())
                .build();
    }

    /**
     * Update existing working hours from request
     */
    private void updateWorkingHoursFromRequest(WorkingHours workingHours, WorkingHoursRequest request) {
        workingHours.setOpeningTime(request.getOpeningTime());
        workingHours.setClosingTime(request.getClosingTime());
        workingHours.setBreakStartTime(request.getBreakStartTime());
        workingHours.setBreakEndTime(request.getBreakEndTime());
        workingHours.setIsClosed(request.getIsClosed());
        workingHours.setIsEmergencyHours(request.getIsEmergencyHours());
        workingHours.setNotes(request.getNotes());
    }

    /**
     * Map WorkingHours entity to WorkingHoursResponse DTO
     */
    private WorkingHoursResponse mapToWorkingHoursResponse(WorkingHours workingHours) {
        return WorkingHoursResponse.builder()
                .id(workingHours.getId())
                .clinicId(workingHours.getClinic().getId())
                .clinicName(workingHours.getClinic().getName())
                .dayOfWeek(workingHours.getDayOfWeek())
                .specificDate(workingHours.getSpecificDate())
                .openingTime(workingHours.getOpeningTime())
                .closingTime(workingHours.getClosingTime())
                .breakStartTime(workingHours.getBreakStartTime())
                .breakEndTime(workingHours.getBreakEndTime())
                .isClosed(workingHours.getIsClosed())
                .isEmergencyHours(workingHours.getIsEmergencyHours())
                .notes(workingHours.getNotes())
                .createdAt(workingHours.getCreatedAt())
                .updatedAt(workingHours.getUpdatedAt())
                .scheduleType(workingHours.isRegularSchedule() ? "REGULAR" : "SPECIFIC_DATE")
                .displaySchedule(workingHours.getEffectiveScheduleDescription())
                .hasBreakTime(workingHours.hasBreakTime())
                .build();
    }
}
