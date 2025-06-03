package press.mizhifei.dentist.clinic.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import press.mizhifei.dentist.clinic.annotation.RequireRoles;
import press.mizhifei.dentist.clinic.client.AuthServiceClient;
import press.mizhifei.dentist.clinic.dto.ApiResponse;
import press.mizhifei.dentist.clinic.dto.ClinicResponse;
import press.mizhifei.dentist.clinic.dto.ClinicSearchRequest;
import press.mizhifei.dentist.clinic.dto.ClinicCreateRequest;
import press.mizhifei.dentist.clinic.dto.PatientWithAppointmentResponse;
import press.mizhifei.dentist.clinic.dto.UserDetailsResponse;
import press.mizhifei.dentist.clinic.model.Role;
import press.mizhifei.dentist.clinic.security.JwtTokenProvider;
import press.mizhifei.dentist.clinic.service.ClinicService;
import press.mizhifei.dentist.clinic.util.JwtUtil;

import java.util.List;
import java.util.Set;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@RestController
@RequestMapping("/clinic")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;
    private final AuthServiceClient authServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> listAllEnabledClinics() {
        List<ClinicResponse> clinics = clinicService.listAllEnabledClinics();
        return ResponseEntity.ok(ApiResponse.success(clinics));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> getClinic(@PathVariable Long id) {
        ClinicResponse response = clinicService.getClinicById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> searchClinics(@Valid @RequestBody ClinicSearchRequest request) {
        List<ClinicResponse> clinics = clinicService.searchClinics(request);
        return ResponseEntity.ok(ApiResponse.success(clinics));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<ClinicResponse>> createClinic(@Valid @RequestBody ClinicCreateRequest request) {
        ClinicResponse response = clinicService.createClinic(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ClinicResponse>> approveClinic(@PathVariable Long id) {
        ClinicResponse response = clinicService.approveClinic(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get patients for a clinic sorted by upcoming appointments
     * Requires CLINIC_ADMIN or RECEPTIONIST role
     * Applies clinic-based filtering for CLINIC_ADMIN and RECEPTIONIST users
     */
    @GetMapping("/{clinicId}/patients")
    @RequireRoles({Role.CLINIC_ADMIN, Role.RECEPTIONIST})
    public ResponseEntity<ApiResponse<List<PatientWithAppointmentResponse>>> getClinicPatients(
            @PathVariable Long clinicId, HttpServletRequest request) {
        try {
            // Extract JWT token from request
            String jwt = JwtUtil.getJwtFromRequest(request);
            if (!StringUtils.hasText(jwt)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication token is required"));
            }

            // Extract user information from token
            String userEmail = jwtTokenProvider.getEmailFromJWT(jwt);
            String rolesString = jwtTokenProvider.getRolesFromJWT(jwt);

            if (!StringUtils.hasText(userEmail) || !StringUtils.hasText(rolesString)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            log.debug("User {} with roles {} requesting patients for clinic {}", userEmail, rolesString, clinicId);

            // Get user details to validate clinic access and roles
            UserDetailsResponse userDetails;
            try {
                userDetails = authServiceClient.getUserDetailsByEmail(userEmail);
            } catch (Exception e) {
                log.error("Failed to fetch user details for email {}: {}", userEmail, e.getMessage());
                return ResponseEntity.status(500)
                        .body(ApiResponse.error("Failed to validate user permissions"));
            }

            // Check if user has required roles (use roles from user details for accuracy)
            Set<String> userRoles = userDetails.roles != null ? userDetails.roles : Set.of();
            boolean hasClinicAdmin = userRoles.contains(Role.CLINIC_ADMIN.name());
            boolean hasReceptionist = userRoles.contains(Role.RECEPTIONIST.name());

            if (!hasClinicAdmin && !hasReceptionist) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Insufficient permissions. CLINIC_ADMIN or RECEPTIONIST role required."));
            }

            // Validate clinic access for CLINIC_ADMIN and RECEPTIONIST users
            if ((hasClinicAdmin || hasReceptionist) && userDetails.clinicId != null) {
                if (!userDetails.clinicId.equals(clinicId)) {
                    return ResponseEntity.status(403)
                            .body(ApiResponse.error("Access denied. You can only view patients from your own clinic."));
                }
            }

            // Get patients sorted by appointments
            List<PatientWithAppointmentResponse> patients = clinicService.getClinicPatientsSortedByAppointments(clinicId);

            log.debug("Returning {} patients for clinic {}", patients.size(), clinicId);
            return ResponseEntity.ok(ApiResponse.success(patients));

        } catch (Exception e) {
            log.error("Error getting patients for clinic {}: {}", clinicId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
}