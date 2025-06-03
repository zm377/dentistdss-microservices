package press.mizhifei.dentist.clinic.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinic.dto.ApiResponse;
import press.mizhifei.dentist.clinic.dto.ServiceRequest;
import press.mizhifei.dentist.clinic.dto.ServiceResponse;
import press.mizhifei.dentist.clinic.service.ServiceManagementService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinic/service")
@RequiredArgsConstructor
public class ServiceManagementController {
    
    private final ServiceManagementService serviceManagementService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceManagementService.createService(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceManagementService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Integer id) {
        serviceManagementService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Service deleted successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getService(@PathVariable Integer id) {
        ServiceResponse response = serviceManagementService.getService(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getClinicServices(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<ServiceResponse> services = serviceManagementService.getClinicServices(clinicId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(services));
    }
    
    @GetMapping("/clinic/{clinicId}/category/{category}")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getServicesByCategory(
            @PathVariable Long clinicId,
            @PathVariable String category) {
        List<ServiceResponse> services = serviceManagementService.getServicesByCategory(clinicId, category);
        return ResponseEntity.ok(ApiResponse.success(services));
    }
    
    @GetMapping("/clinic/{clinicId}/categories")
    public ResponseEntity<ApiResponse<List<String>>> getServiceCategories(@PathVariable Long clinicId) {
        List<String> categories = serviceManagementService.getServiceCategories(clinicId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
} 