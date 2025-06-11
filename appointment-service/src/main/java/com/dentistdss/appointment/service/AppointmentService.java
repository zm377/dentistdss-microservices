package com.dentistdss.appointment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.appointment.client.AuthServiceClient;
import com.dentistdss.appointment.client.ClinicServiceClient;
import com.dentistdss.appointment.client.NotificationClient;
import com.dentistdss.appointment.dto.*;
import com.dentistdss.appointment.model.*;
import com.dentistdss.appointment.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final DentistAvailabilityRepository availabilityRepository;
    private final NotificationClient notificationClient;
    private final AuthServiceClient authServiceClient;
    private final ClinicServiceClient clinicServiceClient;
    
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // Validate appointment doesn't conflict with existing appointments
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.getDentistId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Time slot conflicts with existing appointment");
        }
        
        // Create appointment using saveWithCasting to handle PostgreSQL enum types
        Appointment saved = appointmentRepository.saveWithCasting(
                request.getPatientId(),
                request.getDentistId(),
                request.getClinicId(),
                request.getServiceId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                AppointmentStatus.REQUESTED.name(),
                request.getReasonForVisit(),
                request.getSymptoms(),
                parseUrgencyLevel(request.getUrgencyLevel()).name(),
                null, // aiTriageNotes
                request.getNotes(),
                request.getCreatedBy()
        );
        log.info("Created appointment {} for patient {} with dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        return toResponse(saved);
    }
    
    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId, Long confirmedBy) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new IllegalStateException("Only requested appointments can be confirmed");
        }

        Appointment saved = appointmentRepository.updateStatusWithCasting(
                appointmentId,
                AppointmentStatus.CONFIRMED.name(),
                confirmedBy
        );
        log.info("Confirmed appointment {} by user {}", appointmentId, confirmedBy);
        
        // Send confirmation notification
        try {
            sendAppointmentNotification(saved, "appointment_confirmation");
        } catch (Exception e) {
            log.error("Failed to send confirmation notification: {}", e.getMessage());
        }
        
        return toResponse(saved);
    }
    
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String reason, Long cancelledBy) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }

        Appointment saved = appointmentRepository.updateCancellationWithCasting(
                appointmentId,
                AppointmentStatus.CANCELLED.name(),
                reason,
                cancelledBy
        );
        log.info("Cancelled appointment {} by user {} with reason: {}", 
                appointmentId, cancelledBy, reason);
        
        // Send cancellation notification
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", appointment.getPatientId());
            notificationRequest.put("templateName", "appointment_cancelled");
            notificationRequest.put("type", "EMAIL");

            Map<String, String> templateVariables = new HashMap<>();

            // Fetch actual patient name from auth service
            try {
                String patientName = authServiceClient.getUserFullName(appointment.getPatientId());
                templateVariables.put("patient_name", patientName);
            } catch (Exception e) {
                log.warn("Failed to fetch patient name for cancellation notification: {}", e.getMessage());
                templateVariables.put("patient_name", "Patient");
            }

            templateVariables.put("appointment_date", appointment.getAppointmentDate().toString());
            templateVariables.put("cancellation_reason", reason);
            
            notificationRequest.put("templateVariables", templateVariables);
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send cancellation notification: {}", e.getMessage());
        }
        
        return toResponse(saved);
    }
    
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, 
                                                     LocalDate newDate, 
                                                     LocalTime newStartTime,
                                                     LocalTime newEndTime,
                                                     Long rescheduledBy) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        // Check for conflicts with new time
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                appointment.getDentistId(),
                newDate,
                newStartTime,
                newEndTime
        );
        
        conflicts.removeIf(a -> a.getId().equals(appointmentId)); // Remove self
        
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("New time slot conflicts with existing appointment");
        }
        
        Appointment saved = appointmentRepository.updateScheduleWithCasting(
                appointmentId,
                newDate,
                newStartTime,
                newEndTime,
                AppointmentStatus.RESCHEDULED.name()
        );
        log.info("Rescheduled appointment {} to {} at {} by user {}", 
                appointmentId, newDate, newStartTime, rescheduledBy);
        
        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        List<Appointment> appointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDentistAppointments(Long dentistId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository
                .findByDentistIdAndAppointmentDateOrderByStartTime(dentistId, date);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getClinicAppointments(Long clinicId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository
                .findByClinicIdAndAppointmentDateOrderByStartTime(clinicId, date);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    // Additional methods for inter-service communication
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getLastCompletedAppointmentByPatientAndClinic(Long patientId, Long clinicId, LocalDate currentDate) {
        List<Appointment> appointments = appointmentRepository
                .findLastCompletedAppointmentByPatientAndClinic(patientId, clinicId, currentDate);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getNextUpcomingAppointmentByPatientAndClinic(Long patientId, Long clinicId, LocalDate currentDate, LocalTime currentTime) {
        List<Appointment> appointments = appointmentRepository
                .findNextUpcomingAppointmentByPatientAndClinic(patientId, clinicId, currentDate, currentTime);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Long> getDistinctPatientIdsByClinicId(Long clinicId) {
        return appointmentRepository.findDistinctPatientIdsByClinicId(clinicId);
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long dentistId,
                                                          Long clinicId,
                                                          LocalDate date,
                                                          Integer serviceDurationMinutes) {
        // Get dentist's availability for the date
        List<DentistAvailability> availabilities = availabilityRepository
                .findAvailableSlots(dentistId, clinicId, date);

        // Get existing appointments for the dentist on that date
        List<Appointment> existingAppointments = appointmentRepository
                .findByDentistIdAndAppointmentDateOrderByStartTime(dentistId, date);

        List<AvailableSlotResponse> availableSlots = new ArrayList<>();

        for (DentistAvailability availability : availabilities) {
            LocalTime currentTime = availability.getStartTime();
            LocalTime endTime = availability.getEndTime();

            while (currentTime.plusMinutes(serviceDurationMinutes).isBefore(endTime) ||
                   currentTime.plusMinutes(serviceDurationMinutes).equals(endTime)) {

                LocalTime slotStartTime = currentTime;
                LocalTime slotEndTime = currentTime.plusMinutes(serviceDurationMinutes);

                // Check if this slot conflicts with any existing appointment
                boolean isAvailable = existingAppointments.stream()
                        .noneMatch(apt -> doesTimeOverlap(
                                slotStartTime, slotEndTime,
                                apt.getStartTime(), apt.getEndTime()
                        ));

                if (isAvailable) {
                    availableSlots.add(AvailableSlotResponse.builder()
                            .date(date)
                            .startTime(slotStartTime)
                            .endTime(slotEndTime)
                            .dentistId(dentistId)
                            .clinicId(clinicId)
                            .available(true)
                            .build());
                }

                currentTime = currentTime.plusMinutes(15); // 15-minute increments
            }
        }

        return availableSlots;
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be completed");
        }

        Appointment saved = appointmentRepository.updateStatusOnlyWithCasting(
                appointmentId,
                AppointmentStatus.COMPLETED.name()
        );

        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse markNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        Appointment saved = appointmentRepository.updateStatusOnlyWithCasting(
                appointmentId,
                AppointmentStatus.NO_SHOW.name()
        );

        return toResponse(saved);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .dentistId(appointment.getDentistId())
                .clinicId(appointment.getClinicId())
                .serviceId(appointment.getServiceId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().toString())
                .reasonForVisit(appointment.getReasonForVisit())
                .symptoms(appointment.getSymptoms())
                .urgencyLevel(appointment.getUrgency().toString())
                .aiTriageNotes(appointment.getAiTriageNotes())
                .notes(appointment.getNotes())
                .createdBy(appointment.getCreatedBy())
                .confirmedBy(appointment.getConfirmedBy())
                .cancelledBy(appointment.getCancelledBy())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();

        // Fetch names from user service
        try {
            response.setPatientName(authServiceClient.getUserFullName(appointment.getPatientId()));
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for id {}: {}", appointment.getPatientId(), e.getMessage());
            response.setPatientName("Patient " + appointment.getPatientId());
        }

        try {
            response.setDentistName(authServiceClient.getUserFullName(appointment.getDentistId()));
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for id {}: {}", appointment.getDentistId(), e.getMessage());
            response.setDentistName("Dr. Dentist " + appointment.getDentistId());
        }

        // Fetch clinic name
        if (appointment.getClinicId() != null) {
            try {
                var clinicResponse = clinicServiceClient.getClinic(appointment.getClinicId());
                if (clinicResponse.isSuccess() && clinicResponse.getDataObject() != null) {
                    response.setClinicName(clinicResponse.getDataObject().getName());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch clinic name for id {}: {}", appointment.getClinicId(), e.getMessage());
                response.setClinicName("Clinic " + appointment.getClinicId());
            }
        }

        // Fetch service name
        if (appointment.getServiceId() != null) {
            try {
                var serviceResponse = clinicServiceClient.getService(appointment.getServiceId());
                if (serviceResponse.isSuccess() && serviceResponse.getDataObject() != null) {
                    response.setServiceName(serviceResponse.getDataObject().getName());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch service name for id {}: {}", appointment.getServiceId(), e.getMessage());
                response.setServiceName("Service " + appointment.getServiceId());
            }
        }

        return response;
    }

    private UrgencyLevel parseUrgencyLevel(String level) {
        if (level == null) {
            return UrgencyLevel.ROUTINE;
        }
        try {
            return UrgencyLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UrgencyLevel.ROUTINE;
        }
    }

    private boolean doesTimeOverlap(LocalTime start1, LocalTime end1,
                                     LocalTime start2, LocalTime end2) {
        return (start1.isBefore(end2) && end1.isAfter(start2));
    }

    private void sendAppointmentNotification(Appointment appointment, String templateName) {
        Map<String, Object> notificationRequest = new HashMap<>();
        notificationRequest.put("userId", appointment.getPatientId());
        notificationRequest.put("templateName", templateName);
        notificationRequest.put("type", "EMAIL");

        Map<String, String> templateVariables = new HashMap<>();

        // Fetch actual names from auth service
        try {
            String patientName = authServiceClient.getUserFullName(appointment.getPatientId());
            templateVariables.put("patient_name", patientName);
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for notification: {}", e.getMessage());
            templateVariables.put("patient_name", "Patient");
        }

        try {
            String dentistName = authServiceClient.getUserFullName(appointment.getDentistId());
            templateVariables.put("dentist_name", dentistName);
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for notification: {}", e.getMessage());
            templateVariables.put("dentist_name", "Dr. Dentist");
        }

        templateVariables.put("appointment_date", appointment.getAppointmentDate().toString());
        templateVariables.put("appointment_time", appointment.getStartTime().toString());

        notificationRequest.put("templateVariables", templateVariables);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("appointment_id", appointment.getId());
        notificationRequest.put("metadata", metadata);

        notificationClient.sendNotification(notificationRequest);
    }
}
