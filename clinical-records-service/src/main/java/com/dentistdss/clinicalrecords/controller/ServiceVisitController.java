package com.dentistdss.clinicalrecords.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.clinicalrecords.dto.ApiResponse;
import com.dentistdss.clinicalrecords.dto.ServiceVisitRequest;
import com.dentistdss.clinicalrecords.dto.ServiceVisitResponse;
import com.dentistdss.clinicalrecords.service.ServiceVisitService;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinical-records/visit")
@RequiredArgsConstructor
public class ServiceVisitController {
    
    private final ServiceVisitService serviceVisitService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> createServiceVisit(
            @Valid @RequestBody ServiceVisitRequest request) {
        ServiceVisitResponse response = serviceVisitService.createServiceVisit(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> checkInVisit(@PathVariable Long id) {
        ServiceVisitResponse response = serviceVisitService.checkInVisit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/check-out")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> checkOutVisit(@PathVariable Long id) {
        ServiceVisitResponse response = serviceVisitService.checkOutVisit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> updateVisitNotes(
            @PathVariable Long id,
            @RequestBody String notes) {
        ServiceVisitResponse response = serviceVisitService.updateVisitNotes(id, notes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> cancelVisit(@PathVariable Long id) {
        ServiceVisitResponse response = serviceVisitService.cancelVisit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> getServiceVisit(@PathVariable Long id) {
        ServiceVisitResponse response = serviceVisitService.getServiceVisit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<ServiceVisitResponse>>> getPatientVisits(
            @PathVariable Long patientId) {
        List<ServiceVisitResponse> visits = serviceVisitService.getPatientVisits(patientId);
        return ResponseEntity.ok(ApiResponse.success(visits));
    }
    
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<ApiResponse<List<ServiceVisitResponse>>> getPatientVisitsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ServiceVisitResponse> visits = serviceVisitService.getPatientVisitsByDateRange(patientId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(visits));
    }
    
    @GetMapping("/dentist/{dentistId}")
    public ResponseEntity<ApiResponse<List<ServiceVisitResponse>>> getDentistVisits(
            @PathVariable Long dentistId) {
        List<ServiceVisitResponse> visits = serviceVisitService.getDentistVisits(dentistId);
        return ResponseEntity.ok(ApiResponse.success(visits));
    }
    
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<ServiceVisitResponse>>> getClinicVisits(
            @PathVariable Long clinicId) {
        List<ServiceVisitResponse> visits = serviceVisitService.getClinicVisits(clinicId);
        return ResponseEntity.ok(ApiResponse.success(visits));
    }
    
    @GetMapping("/clinic/{clinicId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ServiceVisitResponse>>> getClinicVisitsByStatus(
            @PathVariable Long clinicId,
            @PathVariable String status) {
        List<ServiceVisitResponse> visits = serviceVisitService.getClinicVisitsByStatus(clinicId, status);
        return ResponseEntity.ok(ApiResponse.success(visits));
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<ServiceVisitResponse>> getVisitByAppointment(
            @PathVariable Long appointmentId) {
        ServiceVisitResponse response = serviceVisitService.getVisitByAppointment(appointmentId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.error("No service visit found for this appointment"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
