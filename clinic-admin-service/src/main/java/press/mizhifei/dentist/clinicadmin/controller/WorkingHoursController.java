package press.mizhifei.dentist.clinicadmin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinicadmin.dto.ApiResponse;
import press.mizhifei.dentist.clinicadmin.dto.WorkingHoursRequest;
import press.mizhifei.dentist.clinicadmin.dto.WorkingHoursResponse;
import press.mizhifei.dentist.clinicadmin.service.WorkingHoursService;
import press.mizhifei.dentist.clinicadmin.util.UserContextUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Working Hours Management Controller
 * 
 * Provides comprehensive working hours management endpoints
 * with proper role-based access control
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    /**
     * Get all working hours for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> getClinicWorkingHours(
            @PathVariable Long clinicId) {
        try {
            List<WorkingHoursResponse> workingHours = workingHoursService.getClinicWorkingHours(clinicId);
            return ResponseEntity.ok(ApiResponse.success(workingHours));
        } catch (Exception e) {
            log.error("Error fetching working hours for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch working hours"));
        }
    }

    /**
     * Get regular weekly working hours for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/regular")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> getRegularWorkingHours(
            @PathVariable Long clinicId) {
        try {
            List<WorkingHoursResponse> workingHours = workingHoursService.getRegularWorkingHours(clinicId);
            return ResponseEntity.ok(ApiResponse.success(workingHours));
        } catch (Exception e) {
            log.error("Error fetching regular working hours for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch regular working hours"));
        }
    }

    /**
     * Get specific date working hours for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/special")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> getSpecialWorkingHours(
            @PathVariable Long clinicId) {
        try {
            List<WorkingHoursResponse> workingHours = workingHoursService.getSpecificDateWorkingHours(clinicId);
            return ResponseEntity.ok(ApiResponse.success(workingHours));
        } catch (Exception e) {
            log.error("Error fetching special working hours for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch special working hours"));
        }
    }

    /**
     * Get working hours for a specific day of week (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> getWorkingHoursForDay(
            @PathVariable Long clinicId,
            @PathVariable DayOfWeek dayOfWeek) {
        try {
            Optional<WorkingHoursResponse> workingHours = workingHoursService.getWorkingHoursForDay(clinicId, dayOfWeek);
            if (workingHours.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(workingHours.get()));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Working hours not found for " + dayOfWeek));
            }
        } catch (Exception e) {
            log.error("Error fetching working hours for clinic {} on {}: {}", clinicId, dayOfWeek, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch working hours"));
        }
    }

    /**
     * Get working hours for a specific date (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/date/{date}")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> getWorkingHoursForDate(
            @PathVariable Long clinicId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Optional<WorkingHoursResponse> workingHours = workingHoursService.getWorkingHoursForDate(clinicId, date);
            if (workingHours.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(workingHours.get()));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Working hours not found for " + date));
            }
        } catch (Exception e) {
            log.error("Error fetching working hours for clinic {} on {}: {}", clinicId, date, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch working hours"));
        }
    }

    /**
     * Check if clinic is open on a specific day (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/is-open/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<Boolean>> isClinicOpenOnDay(
            @PathVariable Long clinicId,
            @PathVariable DayOfWeek dayOfWeek) {
        try {
            boolean isOpen = workingHoursService.isClinicOpen(clinicId, dayOfWeek);
            return ResponseEntity.ok(ApiResponse.success(isOpen));
        } catch (Exception e) {
            log.error("Error checking if clinic {} is open on {}: {}", clinicId, dayOfWeek, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to check clinic availability"));
        }
    }

    /**
     * Check if clinic is open on a specific date (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/working-hours/is-open/date/{date}")
    public ResponseEntity<ApiResponse<Boolean>> isClinicOpenOnDate(
            @PathVariable Long clinicId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            boolean isOpen = workingHoursService.isClinicOpen(clinicId, date);
            return ResponseEntity.ok(ApiResponse.success(isOpen));
        } catch (Exception e) {
            log.error("Error checking if clinic {} is open on {}: {}", clinicId, date, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to check clinic availability"));
        }
    }

    /**
     * Create or update working hours (clinic admin only)
     */
    @PostMapping("/clinics/{clinicId}/working-hours")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> createOrUpdateWorkingHours(
            @PathVariable Long clinicId,
            @Valid @RequestBody WorkingHoursRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(httpRequest);

            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            // Check if user has CLINIC_ADMIN role
            if (!UserContextUtil.isClinicAdmin(httpRequest)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("CLINIC_ADMIN role required"));
            }

            // Validate clinic access for CLINIC_ADMIN users
            if (!UserContextUtil.hasClinicAccess(httpRequest, clinicId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied. You can only manage working hours for your own clinic."));
            }

            log.info("User {} creating/updating working hours for clinic {}", userEmail, clinicId);
            WorkingHoursResponse response = workingHoursService.createOrUpdateWorkingHours(clinicId, request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating/updating working hours for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to save working hours"));
        }
    }

    /**
     * Delete working hours (clinic admin only)
     */
    @DeleteMapping("/clinics/{clinicId}/working-hours/{workingHoursId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkingHours(
            @PathVariable Long clinicId,
            @PathVariable Long workingHoursId,
            HttpServletRequest httpRequest) {
        try {
            // Extract user context from headers (forwarded by API Gateway)
            String userEmail = UserContextUtil.getUserEmail(httpRequest);

            if (!StringUtils.hasText(userEmail)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required"));
            }

            // Check if user has CLINIC_ADMIN role
            if (!UserContextUtil.isClinicAdmin(httpRequest)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("CLINIC_ADMIN role required"));
            }

            // Validate clinic access for CLINIC_ADMIN users
            if (!UserContextUtil.hasClinicAccess(httpRequest, clinicId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied. You can only manage working hours for your own clinic."));
            }

            log.info("User {} deleting working hours {} for clinic {}", userEmail, workingHoursId, clinicId);
            workingHoursService.deleteWorkingHours(clinicId, workingHoursId);
            return ResponseEntity.ok(ApiResponse.successMessage("Working hours deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Working hours not found"));
        } catch (Exception e) {
            log.error("Error deleting working hours {} for clinic {}: {}", workingHoursId, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete working hours"));
        }
    }
}
