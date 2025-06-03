package press.mizhifei.dentist.clinic.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinic.dto.ApiResponse;
import press.mizhifei.dentist.clinic.dto.DentistAvailabilityRequest;
import press.mizhifei.dentist.clinic.dto.DentistAvailabilityResponse;
import press.mizhifei.dentist.clinic.service.DentistAvailabilityService;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinic/dentist-availability")
@RequiredArgsConstructor
public class DentistAvailabilityController {
    
    private final DentistAvailabilityService availabilityService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<List<DentistAvailabilityResponse>>> createAvailability(
            @Valid @RequestBody DentistAvailabilityRequest request) {
        List<DentistAvailabilityResponse> responses = availabilityService.createAvailability(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<DentistAvailabilityResponse>> blockAvailability(
            @PathVariable Integer id,
            @RequestParam String reason) {
        DentistAvailabilityResponse response = availabilityService.blockAvailability(id, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<DentistAvailabilityResponse>> unblockAvailability(
            @PathVariable Integer id) {
        DentistAvailabilityResponse response = availabilityService.unblockAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable Integer id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Availability deleted successfully"));
    }
    
    @GetMapping("/dentist/{dentistId}")
    public ResponseEntity<ApiResponse<List<DentistAvailabilityResponse>>> getDentistAvailability(
            @PathVariable Long dentistId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DentistAvailabilityResponse> responses = availabilityService
                .getDentistAvailability(dentistId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @GetMapping("/dentist/{dentistId}/date/{date}")
    public ResponseEntity<ApiResponse<List<DentistAvailabilityResponse>>> getDentistAvailabilityForDate(
            @PathVariable Long dentistId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DentistAvailabilityResponse> responses = availabilityService
                .getDentistAvailabilityForDate(dentistId, date);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<DentistAvailabilityResponse>>> getAvailableSlots(
            @RequestParam Long dentistId,
            @RequestParam Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DentistAvailabilityResponse> responses = availabilityService
                .getAvailableSlots(dentistId, clinicId, date);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
} 