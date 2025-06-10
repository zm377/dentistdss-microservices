package press.mizhifei.dentist.clinicalrecords.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinicalrecords.dto.ApiResponse;
import press.mizhifei.dentist.clinicalrecords.dto.TreatmentPlanRequest;
import press.mizhifei.dentist.clinicalrecords.dto.TreatmentPlanResponse;
import press.mizhifei.dentist.clinicalrecords.service.TreatmentPlanService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinical-records/treatment-plan")
@RequiredArgsConstructor
public class TreatmentPlanController {
    
    private final TreatmentPlanService treatmentPlanService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> createTreatmentPlan(
            @Valid @RequestBody TreatmentPlanRequest request) {
        TreatmentPlanResponse response = treatmentPlanService.createTreatmentPlan(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> acceptTreatmentPlan(@PathVariable Integer id) {
        TreatmentPlanResponse response = treatmentPlanService.acceptTreatmentPlan(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> startTreatmentPlan(@PathVariable Integer id) {
        TreatmentPlanResponse response = treatmentPlanService.startTreatmentPlan(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> completeTreatmentPlan(@PathVariable Integer id) {
        TreatmentPlanResponse response = treatmentPlanService.completeTreatmentPlan(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{planId}/item/{itemId}/status")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> updateItemStatus(
            @PathVariable Integer planId,
            @PathVariable Integer itemId,
            @RequestParam String status) {
        TreatmentPlanResponse response = treatmentPlanService.updateTreatmentPlanItemStatus(planId, itemId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> getTreatmentPlan(@PathVariable Integer id) {
        TreatmentPlanResponse response = treatmentPlanService.getTreatmentPlan(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<TreatmentPlanResponse>>> getPatientTreatmentPlans(
            @PathVariable Long patientId) {
        List<TreatmentPlanResponse> plans = treatmentPlanService.getPatientTreatmentPlans(patientId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
    
    @GetMapping("/dentist/{dentistId}")
    public ResponseEntity<ApiResponse<List<TreatmentPlanResponse>>> getDentistTreatmentPlans(
            @PathVariable Long dentistId) {
        List<TreatmentPlanResponse> plans = treatmentPlanService.getDentistTreatmentPlans(dentistId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
    
    @GetMapping("/{parentPlanId}/versions")
    public ResponseEntity<ApiResponse<List<TreatmentPlanResponse>>> getPlanVersions(
            @PathVariable Integer parentPlanId) {
        List<TreatmentPlanResponse> plans = treatmentPlanService.getPlanVersions(parentPlanId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
