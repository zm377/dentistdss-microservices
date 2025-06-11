package press.mizhifei.dentist.clinicadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.clinicadmin.dto.HolidayRequest;
import press.mizhifei.dentist.clinicadmin.dto.HolidayResponse;
import press.mizhifei.dentist.clinicadmin.model.Clinic;
import press.mizhifei.dentist.clinicadmin.model.Holiday;
import press.mizhifei.dentist.clinicadmin.repository.ClinicRepository;
import press.mizhifei.dentist.clinicadmin.repository.HolidayRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Holiday Management Service
 * 
 * Handles clinic holiday operations with comprehensive
 * holiday scheduling and management capabilities
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Get all holidays for a clinic
     */
    public List<HolidayResponse> getClinicHolidays(Long clinicId) {
        log.debug("Fetching holidays for clinic ID: {}", clinicId);
        
        List<Holiday> holidays = holidayRepository.findByClinicIdOrderByHolidayDateAsc(clinicId);
        return holidays.stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming holidays for a clinic
     */
    public List<HolidayResponse> getUpcomingHolidays(Long clinicId) {
        log.debug("Fetching upcoming holidays for clinic ID: {}", clinicId);
        
        List<Holiday> holidays = holidayRepository.findUpcomingHolidays(clinicId);
        return holidays.stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get holidays by type
     */
    public List<HolidayResponse> getHolidaysByType(Long clinicId, Holiday.HolidayType type) {
        log.debug("Fetching holidays of type {} for clinic ID: {}", type, clinicId);
        
        List<Holiday> holidays = holidayRepository.findByClinicIdAndTypeOrderByHolidayDateAsc(clinicId, type);
        return holidays.stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new holiday
     */
    @Transactional
    public HolidayResponse createHoliday(Long clinicId, HolidayRequest request) {
        log.info("Creating holiday for clinic ID: {}", clinicId);
        
        // Verify clinic exists
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + clinicId));

        // Validate request
        validateHolidayRequest(request);

        // Check if holiday already exists for this date
        Optional<Holiday> existingHoliday = holidayRepository.findByClinicIdAndHolidayDate(clinicId, request.getHolidayDate());
        if (existingHoliday.isPresent()) {
            throw new IllegalArgumentException("Holiday already exists for date: " + request.getHolidayDate());
        }

        Holiday holiday = Holiday.builder()
                .clinic(clinic)
                .name(request.getName())
                .holidayDate(request.getHolidayDate())
                .description(request.getDescription())
                .type(request.getType())
                .isFullDayClosure(request.getIsFullDayClosure())
                .specialOpeningTime(request.getSpecialOpeningTime())
                .specialClosingTime(request.getSpecialClosingTime())
                .isRecurring(request.getIsRecurring())
                .emergencyContact(request.getEmergencyContact())
                .build();

        Holiday savedHoliday = holidayRepository.save(holiday);
        log.info("Created holiday with ID: {} for clinic ID: {}", savedHoliday.getId(), clinicId);
        
        return mapToHolidayResponse(savedHoliday);
    }

    /**
     * Update an existing holiday
     */
    @Transactional
    public HolidayResponse updateHoliday(Long clinicId, Long holidayId, HolidayRequest request) {
        log.info("Updating holiday with ID: {} for clinic ID: {}", holidayId, clinicId);
        
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with ID: " + holidayId));

        if (!holiday.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("Holiday does not belong to the specified clinic");
        }

        // Validate request
        validateHolidayRequest(request);

        // Check if date is being changed and if it conflicts
        if (!holiday.getHolidayDate().equals(request.getHolidayDate())) {
            Optional<Holiday> existingHoliday = holidayRepository.findByClinicIdAndHolidayDate(clinicId, request.getHolidayDate());
            if (existingHoliday.isPresent() && !existingHoliday.get().getId().equals(holidayId)) {
                throw new IllegalArgumentException("Holiday already exists for date: " + request.getHolidayDate());
            }
        }

        // Update holiday fields
        holiday.setName(request.getName());
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setDescription(request.getDescription());
        holiday.setType(request.getType());
        holiday.setIsFullDayClosure(request.getIsFullDayClosure());
        holiday.setSpecialOpeningTime(request.getSpecialOpeningTime());
        holiday.setSpecialClosingTime(request.getSpecialClosingTime());
        holiday.setIsRecurring(request.getIsRecurring());
        holiday.setEmergencyContact(request.getEmergencyContact());

        Holiday updatedHoliday = holidayRepository.save(holiday);
        log.info("Updated holiday with ID: {}", updatedHoliday.getId());
        
        return mapToHolidayResponse(updatedHoliday);
    }

    /**
     * Delete a holiday
     */
    @Transactional
    public void deleteHoliday(Long clinicId, Long holidayId) {
        log.info("Deleting holiday with ID: {} for clinic ID: {}", holidayId, clinicId);
        
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with ID: " + holidayId));

        if (!holiday.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("Holiday does not belong to the specified clinic");
        }

        holidayRepository.delete(holiday);
        log.info("Deleted holiday with ID: {}", holidayId);
    }

    /**
     * Check if a date is a holiday for a clinic
     */
    public boolean isHoliday(Long clinicId, LocalDate date) {
        List<Holiday> holidays = holidayRepository.findHolidaysAffectingDate(clinicId, date);
        return !holidays.isEmpty();
    }

    /**
     * Get holidays affecting a specific date
     */
    public List<HolidayResponse> getHolidaysAffectingDate(Long clinicId, LocalDate date) {
        log.debug("Fetching holidays affecting date {} for clinic ID: {}", date, clinicId);
        
        List<Holiday> holidays = holidayRepository.findHolidaysAffectingDate(clinicId, date);
        return holidays.stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get holidays in a date range
     */
    public List<HolidayResponse> getHolidaysInRange(Long clinicId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching holidays between {} and {} for clinic ID: {}", startDate, endDate, clinicId);
        
        List<Holiday> holidays = holidayRepository.findByClinicIdAndDateRange(clinicId, startDate, endDate);
        return holidays.stream()
                .map(this::mapToHolidayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate holiday request
     */
    private void validateHolidayRequest(HolidayRequest request) {
        // If not full day closure, special hours must be provided
        if (!request.getIsFullDayClosure()) {
            if (request.getSpecialOpeningTime() == null || request.getSpecialClosingTime() == null) {
                throw new IllegalArgumentException("Special opening and closing times must be provided when not a full day closure");
            }
            if (request.getSpecialOpeningTime().isAfter(request.getSpecialClosingTime()) || 
                request.getSpecialOpeningTime().equals(request.getSpecialClosingTime())) {
                throw new IllegalArgumentException("Special opening time must be before special closing time");
            }
        }

        // Holiday date cannot be in the past (except for emergency closures)
        if (request.getType() != Holiday.HolidayType.EMERGENCY_CLOSURE && 
            request.getHolidayDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Holiday date cannot be in the past (except for emergency closures)");
        }
    }

    /**
     * Map Holiday entity to HolidayResponse DTO
     */
    private HolidayResponse mapToHolidayResponse(Holiday holiday) {
        LocalDate today = LocalDate.now();
        boolean isUpcoming = holiday.getHolidayDate().isAfter(today);
        Long daysUntil = isUpcoming ? ChronoUnit.DAYS.between(today, holiday.getHolidayDate()) : null;

        return HolidayResponse.builder()
                .id(holiday.getId())
                .clinicId(holiday.getClinic().getId())
                .clinicName(holiday.getClinic().getName())
                .name(holiday.getName())
                .holidayDate(holiday.getHolidayDate())
                .description(holiday.getDescription())
                .type(holiday.getType())
                .typeDisplayName(holiday.getType().getDisplayName())
                .isFullDayClosure(holiday.getIsFullDayClosure())
                .specialOpeningTime(holiday.getSpecialOpeningTime())
                .specialClosingTime(holiday.getSpecialClosingTime())
                .isRecurring(holiday.getIsRecurring())
                .emergencyContact(holiday.getEmergencyContact())
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .displayInfo(holiday.getDisplayInfo())
                .hasSpecialHours(holiday.hasSpecialHours())
                .isUpcoming(isUpcoming)
                .daysUntilHoliday(daysUntil)
                .build();
    }
}
