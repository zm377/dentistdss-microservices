package press.mizhifei.dentist.clinicadmin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinicadmin.dto.*;
import press.mizhifei.dentist.clinicadmin.service.ClinicAdminService;
import press.mizhifei.dentist.clinicadmin.util.UserContextUtil;

import java.util.List;

/**
 * Enhanced Clinic Administration Controller
 * 
 * Provides comprehensive clinic management endpoints with proper
 * role-based access control and SOLID principles implementation
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class ClinicAdminController {

    private final ClinicAdminService clinicAdminService;

    /**
     * Get all enabled clinics (public endpoint)
     */
    @GetMapping("/clinics")
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> listAllEnabledClinics() {
        log.debug("Fetching all enabled clinics");
        List<ClinicResponse> clinics = clinicAdminService.listAllEnabledClinics();
        return ResponseEntity.ok(ApiResponse.success(clinics));
    }

    /**
     * Get clinic by ID (public endpoint)
     */
    @GetMapping("/clinics/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> getClinic(@PathVariable Long id) {
        try {
            ClinicResponse response = clinicAdminService.getClinicById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Clinic not found"));
        } catch (Exception e) {
            log.error("Error fetching clinic {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Search clinics (public endpoint)
     */
    @PostMapping("/clinics/search")
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> searchClinics(
            @Valid @RequestBody ClinicSearchRequest request) {
        try {
            List<ClinicResponse> clinics = clinicAdminService.searchClinics(request);
            return ResponseEntity.ok(ApiResponse.success(clinics));
        } catch (Exception e) {
            log.error("Error searching clinics: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Create new clinic (authenticated users only)
     */
    @PostMapping("/clinics")
    public ResponseEntity<ApiResponse<ClinicResponse>> createClinic(
            @Valid @RequestBody ClinicCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(httpRequest);
            
            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            log.info("User {} creating new clinic: {}", userEmail, request.getName());
            ClinicResponse response = clinicAdminService.createClinic(request);
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating clinic: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to create clinic"));
        }
    }

    /**
     * Update clinic information (clinic admin only)
     */
    @PutMapping("/clinics/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> updateClinic(
            @PathVariable Long id,
            @Valid @RequestBody ClinicUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(httpRequest);
            String userId = UserContextUtil.getUserId(httpRequest);

            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            // Check if user has CLINIC_ADMIN role
            if (!UserContextUtil.isClinicAdmin(httpRequest)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("CLINIC_ADMIN role required"));
            }

            log.info("User {} updating clinic {}", userEmail, id);

            // Validate clinic access for CLINIC_ADMIN users
            if (!UserContextUtil.hasClinicAccess(httpRequest, id)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied. You can only update your own clinic."));
            }

            ClinicResponse response = clinicAdminService.updateClinic(id, request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Clinic not found"));
        } catch (Exception e) {
            log.error("Error updating clinic {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update clinic"));
        }
    }

    /**
     * Approve clinic (system admin only)
     */
    @PatchMapping("/clinics/{id}/approve")
    public ResponseEntity<ApiResponse<ClinicResponse>> approveClinic(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(httpRequest);

            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            // Check if user has SYSTEM_ADMIN role
            if (!UserContextUtil.isSystemAdmin(httpRequest)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("SYSTEM_ADMIN role required"));
            }

            log.info("System admin {} approving clinic {}", userEmail, id);
            ClinicResponse response = clinicAdminService.approveClinic(id);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Clinic not found"));
        } catch (Exception e) {
            log.error("Error approving clinic {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to approve clinic"));
        }
    }

    /**
     * Get patients for a clinic sorted by upcoming appointments
     * Requires CLINIC_ADMIN or RECEPTIONIST role
     */
    @GetMapping("/clinics/{clinicId}/patients")
    public ResponseEntity<ApiResponse<List<PatientWithAppointmentResponse>>> getClinicPatients(
            @PathVariable Long clinicId, 
            HttpServletRequest request) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(request);

            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            log.debug("User {} requesting patients for clinic {}", userEmail, clinicId);

            // Check if user has required roles
            if (!UserContextUtil.isClinicStaff(request)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("CLINIC_ADMIN or RECEPTIONIST role required"));
            }

            // Validate clinic access for CLINIC_ADMIN and RECEPTIONIST users
            if (!UserContextUtil.hasClinicAccess(request, clinicId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied. You can only view patients from your own clinic."));
            }

            // Get patients sorted by appointments
            List<PatientWithAppointmentResponse> patients = clinicAdminService.getClinicPatientsSortedByAppointments(clinicId);

            log.debug("Returning {} patients for clinic {}", patients.size(), clinicId);
            return ResponseEntity.ok(ApiResponse.success(patients));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Clinic not found"));
        } catch (Exception e) {
            log.error("Error getting patients for clinic {}: {}", clinicId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    /**
     * Get dentists for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/dentists")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getClinicDentists(@PathVariable Long clinicId) {
        try {
            List<UserResponse> dentists = clinicAdminService.getClinicDentists(clinicId);
            return ResponseEntity.ok(ApiResponse.success(dentists));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Clinic not found"));
        } catch (Exception e) {
            log.error("Error getting dentists for clinic {}: {}", clinicId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
}
