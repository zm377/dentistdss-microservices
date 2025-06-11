package com.dentistdss.appointment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.appointment.dto.*;
import com.dentistdss.appointment.service.AppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @PathVariable Long id, @RequestParam Long confirmedBy) {
        AppointmentResponse response = appointmentService.confirmAppointment(id, confirmedBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledBy) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, reason, cancelledBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newEndTime,
            @RequestParam Long rescheduledBy) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(
                id, newDate, newStartTime, newEndTime, rescheduledBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}/no-show")
    public ResponseEntity<ApiResponse<AppointmentResponse>> markNoShow(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.markNoShow(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPatientAppointments(
            @PathVariable Long patientId) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/dentist/{dentistId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDentistAppointments(
            @PathVariable Long dentistId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.getDentistAppointments(dentistId, date);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getClinicAppointments(
            @PathVariable Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.getClinicAppointments(clinicId, date);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @RequestParam Long dentistId,
            @RequestParam Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") Integer serviceDurationMinutes) {
        List<AvailableSlotResponse> slots = appointmentService.getAvailableSlots(
                dentistId, clinicId, date, serviceDurationMinutes);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
    
    // Additional endpoints for inter-service communication
    @GetMapping("/patient/{patientId}/clinic/{clinicId}/last-completed")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getLastCompletedAppointment(
            @PathVariable Long patientId,
            @PathVariable Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate) {
        List<AppointmentResponse> appointments = appointmentService.getLastCompletedAppointmentByPatientAndClinic(
                patientId, clinicId, currentDate);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/patient/{patientId}/clinic/{clinicId}/next-upcoming")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getNextUpcomingAppointment(
            @PathVariable Long patientId,
            @PathVariable Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime currentTime) {
        List<AppointmentResponse> appointments = appointmentService.getNextUpcomingAppointmentByPatientAndClinic(
                patientId, clinicId, currentDate, currentTime);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/clinic/{clinicId}/patients")
    public ResponseEntity<ApiResponse<List<Long>>> getClinicPatientIds(@PathVariable Long clinicId) {
        List<Long> patientIds = appointmentService.getDistinctPatientIdsByClinicId(clinicId);
        return ResponseEntity.ok(ApiResponse.success(patientIds));
    }
}
