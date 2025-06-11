package com.dentistdss.clinicadmin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.clinicadmin.dto.ApiResponse;
import com.dentistdss.clinicadmin.dto.HolidayRequest;
import com.dentistdss.clinicadmin.dto.HolidayResponse;
import com.dentistdss.clinicadmin.model.Holiday;
import com.dentistdss.clinicadmin.service.HolidayService;
import com.dentistdss.clinicadmin.util.UserContextUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Holiday Management Controller
 * 
 * Provides comprehensive holiday management endpoints
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
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * Get all holidays for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getClinicHolidays(
            @PathVariable Long clinicId) {
        try {
            List<HolidayResponse> holidays = holidayService.getClinicHolidays(clinicId);
            return ResponseEntity.ok(ApiResponse.success(holidays));
        } catch (Exception e) {
            log.error("Error fetching holidays for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch holidays"));
        }
    }

    /**
     * Get upcoming holidays for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays/upcoming")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getUpcomingHolidays(
            @PathVariable Long clinicId) {
        try {
            List<HolidayResponse> holidays = holidayService.getUpcomingHolidays(clinicId);
            return ResponseEntity.ok(ApiResponse.success(holidays));
        } catch (Exception e) {
            log.error("Error fetching upcoming holidays for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch upcoming holidays"));
        }
    }

    /**
     * Get holidays by type for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays/type/{type}")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidaysByType(
            @PathVariable Long clinicId,
            @PathVariable Holiday.HolidayType type) {
        try {
            List<HolidayResponse> holidays = holidayService.getHolidaysByType(clinicId, type);
            return ResponseEntity.ok(ApiResponse.success(holidays));
        } catch (Exception e) {
            log.error("Error fetching holidays of type {} for clinic {}: {}", type, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch holidays"));
        }
    }

    /**
     * Check if a date is a holiday for a clinic (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays/is-holiday/{date}")
    public ResponseEntity<ApiResponse<Boolean>> isHoliday(
            @PathVariable Long clinicId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            boolean isHoliday = holidayService.isHoliday(clinicId, date);
            return ResponseEntity.ok(ApiResponse.success(isHoliday));
        } catch (Exception e) {
            log.error("Error checking if {} is a holiday for clinic {}: {}", date, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to check holiday status"));
        }
    }

    /**
     * Get holidays affecting a specific date (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays/affecting/{date}")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidaysAffectingDate(
            @PathVariable Long clinicId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<HolidayResponse> holidays = holidayService.getHolidaysAffectingDate(clinicId, date);
            return ResponseEntity.ok(ApiResponse.success(holidays));
        } catch (Exception e) {
            log.error("Error fetching holidays affecting {} for clinic {}: {}", date, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch holidays"));
        }
    }

    /**
     * Get holidays in a date range (public endpoint)
     */
    @GetMapping("/clinics/{clinicId}/holidays/range")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidaysInRange(
            @PathVariable Long clinicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Start date must be before or equal to end date"));
            }

            List<HolidayResponse> holidays = holidayService.getHolidaysInRange(clinicId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(holidays));
        } catch (Exception e) {
            log.error("Error fetching holidays in range for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch holidays"));
        }
    }

    /**
     * Create a new holiday (clinic admin only)
     */
    @PostMapping("/clinics/{clinicId}/holidays")
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(
            @PathVariable Long clinicId,
            @Valid @RequestBody HolidayRequest request,
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
                        .body(ApiResponse.error("Access denied. You can only manage holidays for your own clinic."));
            }

            log.info("User {} creating holiday for clinic {}: {}", userEmail, clinicId, request.getName());
            HolidayResponse response = holidayService.createHoliday(clinicId, request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating holiday for clinic {}: {}", clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to create holiday"));
        }
    }

    /**
     * Update an existing holiday (clinic admin only)
     */
    @PutMapping("/clinics/{clinicId}/holidays/{holidayId}")
    public ResponseEntity<ApiResponse<HolidayResponse>> updateHoliday(
            @PathVariable Long clinicId,
            @PathVariable Long holidayId,
            @Valid @RequestBody HolidayRequest request,
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
                        .body(ApiResponse.error("Access denied. You can only manage holidays for your own clinic."));
            }

            log.info("User {} updating holiday {} for clinic {}", userEmail, holidayId, clinicId);
            HolidayResponse response = holidayService.updateHoliday(clinicId, holidayId, request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Holiday not found"));
        } catch (Exception e) {
            log.error("Error updating holiday {} for clinic {}: {}", holidayId, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update holiday"));
        }
    }

    /**
     * Delete a holiday (clinic admin only)
     */
    @DeleteMapping("/clinics/{clinicId}/holidays/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(
            @PathVariable Long clinicId,
            @PathVariable Long holidayId,
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
                        .body(ApiResponse.error("Access denied. You can only manage holidays for your own clinic."));
            }

            log.info("User {} deleting holiday {} for clinic {}", userEmail, holidayId, clinicId);
            holidayService.deleteHoliday(clinicId, holidayId);
            return ResponseEntity.ok(ApiResponse.successMessage("Holiday deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Holiday not found"));
        } catch (Exception e) {
            log.error("Error deleting holiday {} for clinic {}: {}", holidayId, clinicId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete holiday"));
        }
    }
}
