package press.mizhifei.dentist.clinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.clinic.dto.DentistAvailabilityRequest;
import press.mizhifei.dentist.clinic.dto.DentistAvailabilityResponse;
import press.mizhifei.dentist.clinic.model.DentistAvailability;
import press.mizhifei.dentist.clinic.repository.DentistAvailabilityRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DentistAvailabilityService {
    
    private final DentistAvailabilityRepository availabilityRepository;
    
    @Transactional
    public List<DentistAvailabilityResponse> createAvailability(DentistAvailabilityRequest request) {
        List<DentistAvailability> createdSlots = new ArrayList<>();
        
        if (request.getIsRecurring()) {
            // Create recurring availability slots
            LocalDate startDate = request.getEffectiveFrom() != null ? 
                    request.getEffectiveFrom() : LocalDate.now();
            LocalDate endDate = request.getEffectiveUntil() != null ? 
                    request.getEffectiveUntil() : startDate.plusMonths(3);
            
            // Find the first occurrence of the specified day of week
            DayOfWeek targetDay = DayOfWeek.of(request.getDayOfWeek() == 0 ? 7 : request.getDayOfWeek());
            LocalDate currentDate = startDate.with(TemporalAdjusters.nextOrSame(targetDay));
            
            while (!currentDate.isAfter(endDate)) {
                DentistAvailability availability = DentistAvailability.builder()
                        .dentistId(request.getDentistId())
                        .clinicId(request.getClinicId())
                        .availableDate(currentDate)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .isBlocked(false)
                        .build();
                
                createdSlots.add(availability);
                currentDate = currentDate.plusWeeks(1);
            }
        } else {
            // Create single availability slot
            LocalDate availableDate = request.getEffectiveFrom() != null ? 
                    request.getEffectiveFrom() : LocalDate.now();
            
            DentistAvailability availability = DentistAvailability.builder()
                    .dentistId(request.getDentistId())
                    .clinicId(request.getClinicId())
                    .availableDate(availableDate)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .isBlocked(false)
                    .build();
            
            createdSlots.add(availability);
        }
        
        List<DentistAvailability> savedSlots = availabilityRepository.saveAll(createdSlots);
        log.info("Created {} availability slots for dentist {}", savedSlots.size(), request.getDentistId());
        
        return savedSlots.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional
    public DentistAvailabilityResponse blockAvailability(Integer id, String blockReason) {
        DentistAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found"));
        
        availability.setIsBlocked(true);
        availability.setBlockReason(blockReason);
        
        DentistAvailability saved = availabilityRepository.save(availability);
        log.info("Blocked availability slot {} for reason: {}", id, blockReason);
        
        return toResponse(saved);
    }
    
    @Transactional
    public DentistAvailabilityResponse unblockAvailability(Integer id) {
        DentistAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found"));
        
        availability.setIsBlocked(false);
        availability.setBlockReason(null);
        
        DentistAvailability saved = availabilityRepository.save(availability);
        log.info("Unblocked availability slot {}", id);
        
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteAvailability(Integer id) {
        availabilityRepository.deleteById(id);
        log.info("Deleted availability slot {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<DentistAvailabilityResponse> getDentistAvailability(Long dentistId, LocalDate startDate, LocalDate endDate) {
        List<DentistAvailability> availabilities = availabilityRepository
                .findByDentistIdAndAvailableDateBetween(dentistId, startDate, endDate);
        
        return availabilities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentistAvailabilityResponse> getDentistAvailabilityForDate(Long dentistId, LocalDate date) {
        List<DentistAvailability> availabilities = availabilityRepository
                .findByDentistIdAndAvailableDate(dentistId, date);
        
        return availabilities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentistAvailabilityResponse> getAvailableSlots(Long dentistId, Long clinicId, LocalDate date) {
        List<DentistAvailability> availabilities = availabilityRepository
                .findAvailableSlots(dentistId, clinicId, date);
        
        return availabilities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isDentistAvailable(Long dentistId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<DentistAvailability> availabilities = availabilityRepository
                .findByDentistIdAndAvailableDate(dentistId, date);
        
        return availabilities.stream().anyMatch(avail -> 
            !avail.getIsBlocked() &&
            !startTime.isBefore(avail.getStartTime()) && 
            !endTime.isAfter(avail.getEndTime())
        );
    }
    
    private DentistAvailabilityResponse toResponse(DentistAvailability availability) {
        return DentistAvailabilityResponse.builder()
                .id(availability.getId())
                .dentistId(availability.getDentistId())
                .clinicId(availability.getClinicId())
                .dayOfWeek(availability.getAvailableDate().getDayOfWeek().getValue() % 7)
                .dayName(availability.getAvailableDate().getDayOfWeek().toString())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .isRecurring(false) // This is always false for individual slots
                .effectiveFrom(availability.getAvailableDate())
                .effectiveUntil(availability.getAvailableDate())
                .isActive(!availability.getIsBlocked())
                .build();
    }
} 