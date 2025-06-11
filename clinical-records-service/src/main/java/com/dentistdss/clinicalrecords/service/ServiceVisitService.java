package com.dentistdss.clinicalrecords.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.clinicalrecords.client.AuthServiceClient;
import com.dentistdss.clinicalrecords.client.ClinicServiceClient;
import com.dentistdss.clinicalrecords.dto.ServiceVisitRequest;
import com.dentistdss.clinicalrecords.dto.ServiceVisitResponse;
import com.dentistdss.clinicalrecords.model.ServiceVisit;
import com.dentistdss.clinicalrecords.repository.ServiceVisitRepository;

import java.time.Duration;
import java.time.LocalDateTime;
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
public class ServiceVisitService {
    
    private final ServiceVisitRepository serviceVisitRepository;
    private final AuthServiceClient authServiceClient;
    private final ClinicServiceClient clinicServiceClient;
    
    @Transactional
    public ServiceVisitResponse createServiceVisit(ServiceVisitRequest request) {
        ServiceVisit serviceVisit = ServiceVisit.builder()
                .patientId(request.getPatientId())
                .dentistId(request.getDentistId())
                .clinicId(request.getClinicId())
                .appointmentId(request.getAppointmentId())
                .visitType(request.getVisitType())
                .visitDate(request.getVisitDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .notes(request.getNotes())
                .build();
        
        // Calculate duration if both check-in and check-out times are provided
        if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
            Duration duration = Duration.between(request.getCheckInTime(), request.getCheckOutTime());
            serviceVisit.setDurationMinutes((int) duration.toMinutes());
            serviceVisit.setStatus("COMPLETED");
        }
        
        ServiceVisit saved = serviceVisitRepository.save(serviceVisit);
        log.info("Created service visit {} for patient {} by dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ServiceVisitResponse checkInVisit(Long visitId) {
        ServiceVisit visit = serviceVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Service visit not found"));
        
        if (visit.getCheckInTime() != null) {
            throw new IllegalStateException("Visit is already checked in");
        }
        
        visit.setCheckInTime(LocalDateTime.now());
        visit.setStatus("IN_PROGRESS");
        
        ServiceVisit saved = serviceVisitRepository.save(visit);
        log.info("Checked in service visit {}", visitId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public ServiceVisitResponse checkOutVisit(Long visitId) {
        ServiceVisit visit = serviceVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Service visit not found"));
        
        if (visit.getCheckInTime() == null) {
            throw new IllegalStateException("Visit must be checked in before check out");
        }
        
        if (visit.getCheckOutTime() != null) {
            throw new IllegalStateException("Visit is already checked out");
        }
        
        LocalDateTime checkOutTime = LocalDateTime.now();
        visit.setCheckOutTime(checkOutTime);
        visit.setStatus("COMPLETED");
        
        // Calculate duration
        Duration duration = Duration.between(visit.getCheckInTime(), checkOutTime);
        visit.setDurationMinutes((int) duration.toMinutes());
        
        ServiceVisit saved = serviceVisitRepository.save(visit);
        log.info("Checked out service visit {} with duration {} minutes", visitId, saved.getDurationMinutes());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ServiceVisitResponse updateVisitNotes(Long visitId, String notes) {
        ServiceVisit visit = serviceVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Service visit not found"));
        
        visit.setNotes(notes);
        ServiceVisit saved = serviceVisitRepository.save(visit);
        log.info("Updated notes for service visit {}", visitId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public ServiceVisitResponse cancelVisit(Long visitId) {
        ServiceVisit visit = serviceVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Service visit not found"));
        
        if ("COMPLETED".equals(visit.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed visit");
        }
        
        visit.setStatus("CANCELLED");
        ServiceVisit saved = serviceVisitRepository.save(visit);
        log.info("Cancelled service visit {}", visitId);
        
        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public ServiceVisitResponse getServiceVisit(Long visitId) {
        ServiceVisit visit = serviceVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Service visit not found"));
        
        return toResponse(visit);
    }
    
    @Transactional(readOnly = true)
    public List<ServiceVisitResponse> getPatientVisits(Long patientId) {
        List<ServiceVisit> visits = serviceVisitRepository.findByPatientIdOrderByVisitDateDesc(patientId);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ServiceVisitResponse> getDentistVisits(Long dentistId) {
        List<ServiceVisit> visits = serviceVisitRepository.findByDentistIdOrderByVisitDateDesc(dentistId);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ServiceVisitResponse> getClinicVisits(Long clinicId) {
        List<ServiceVisit> visits = serviceVisitRepository.findByClinicIdOrderByVisitDateDesc(clinicId);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ServiceVisitResponse getVisitByAppointment(Long appointmentId) {
        ServiceVisit visit = serviceVisitRepository.findByAppointmentId(appointmentId)
                .orElse(null);
        
        return visit != null ? toResponse(visit) : null;
    }
    
    @Transactional(readOnly = true)
    public List<ServiceVisitResponse> getPatientVisitsByDateRange(Long patientId, 
                                                                 LocalDateTime startDate, 
                                                                 LocalDateTime endDate) {
        List<ServiceVisit> visits = serviceVisitRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ServiceVisitResponse> getClinicVisitsByStatus(Long clinicId, String status) {
        List<ServiceVisit> visits = serviceVisitRepository.findByClinicIdAndStatus(clinicId, status);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    private ServiceVisitResponse toResponse(ServiceVisit visit) {
        ServiceVisitResponse response = ServiceVisitResponse.builder()
                .id(visit.getId())
                .patientId(visit.getPatientId())
                .dentistId(visit.getDentistId())
                .clinicId(visit.getClinicId())
                .appointmentId(visit.getAppointmentId())
                .visitType(visit.getVisitType())
                .visitDate(visit.getVisitDate())
                .checkInTime(visit.getCheckInTime())
                .checkOutTime(visit.getCheckOutTime())
                .durationMinutes(visit.getDurationMinutes())
                .status(visit.getStatus())
                .notes(visit.getNotes())
                .createdAt(visit.getCreatedAt())
                .updatedAt(visit.getUpdatedAt())
                .build();
        
        // Fetch names from services
        try {
            response.setPatientName(authServiceClient.getUserFullName(visit.getPatientId()));
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for id {}: {}", visit.getPatientId(), e.getMessage());
        }
        
        try {
            response.setDentistName(authServiceClient.getUserFullName(visit.getDentistId()));
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for id {}: {}", visit.getDentistId(), e.getMessage());
        }
        
        try {
            response.setClinicName(clinicServiceClient.getClinic(visit.getClinicId()).getName());
        } catch (Exception e) {
            log.warn("Failed to fetch clinic name for id {}: {}", visit.getClinicId(), e.getMessage());
        }
        
        return response;
    }
}
