package press.mizhifei.dentist.clinicadmin.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for extracting user context from HTTP headers
 * Used by downstream services to get user information forwarded from API Gateway
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
public class UserContextUtil {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String CLINIC_ID_HEADER = "X-Clinic-ID";

    /**
     * Extract user ID from request headers
     */
    public static String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.hasText(userId) ? userId : null;
    }

    /**
     * Extract user email from request headers
     */
    public static String getUserEmail(HttpServletRequest request) {
        String email = request.getHeader(USER_EMAIL_HEADER);
        return StringUtils.hasText(email) ? email : null;
    }

    /**
     * Extract user roles from request headers
     */
    public static List<String> getUserRoles(HttpServletRequest request) {
        String roles = request.getHeader(USER_ROLES_HEADER);
        if (StringUtils.hasText(roles)) {
            return Arrays.asList(roles.split(","));
        }
        return List.of();
    }

    /**
     * Extract user roles as a Set from request headers
     */
    public static Set<String> getUserRolesSet(HttpServletRequest request) {
        return getUserRoles(request).stream().collect(Collectors.toSet());
    }

    /**
     * Extract clinic ID from request headers
     */
    public static String getClinicId(HttpServletRequest request) {
        String clinicId = request.getHeader(CLINIC_ID_HEADER);
        return StringUtils.hasText(clinicId) ? clinicId : null;
    }

    /**
     * Extract clinic ID as Long from request headers
     */
    public static Long getClinicIdAsLong(HttpServletRequest request) {
        String clinicId = getClinicId(request);
        if (clinicId != null) {
            try {
                return Long.parseLong(clinicId);
            } catch (NumberFormatException e) {
                log.warn("Invalid clinic ID format: {}", clinicId);
            }
        }
        return null;
    }

    /**
     * Check if user has a specific role
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        return getUserRoles(request).contains(role);
    }

    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        Set<String> userRoles = getUserRolesSet(request);
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    /**
     * Check if user is a system admin
     */
    public static boolean isSystemAdmin(HttpServletRequest request) {
        return hasRole(request, "SYSTEM_ADMIN");
    }

    /**
     * Check if user is a clinic admin
     */
    public static boolean isClinicAdmin(HttpServletRequest request) {
        return hasRole(request, "CLINIC_ADMIN");
    }

    /**
     * Check if user is a receptionist
     */
    public static boolean isReceptionist(HttpServletRequest request) {
        return hasRole(request, "RECEPTIONIST");
    }

    /**
     * Check if user is a dentist
     */
    public static boolean isDentist(HttpServletRequest request) {
        return hasRole(request, "DENTIST");
    }

    /**
     * Check if user is a patient
     */
    public static boolean isPatient(HttpServletRequest request) {
        return hasRole(request, "PATIENT");
    }

    /**
     * Check if user has clinic admin or receptionist role
     */
    public static boolean isClinicStaff(HttpServletRequest request) {
        return hasAnyRole(request, "CLINIC_ADMIN", "RECEPTIONIST");
    }

    /**
     * Validate clinic access for clinic-specific operations
     * Returns true if user is system admin or belongs to the specified clinic
     */
    public static boolean hasClinicAccess(HttpServletRequest request, Long clinicId) {
        if (isSystemAdmin(request)) {
            return true;
        }

        Long userClinicId = getClinicIdAsLong(request);
        return userClinicId != null && userClinicId.equals(clinicId);
    }

    /**
     * Get user context summary for logging
     */
    public static String getUserContextSummary(HttpServletRequest request) {
        return String.format("User[ID=%s, Email=%s, Roles=%s, ClinicId=%s]",
            getUserId(request),
            getUserEmail(request),
            String.join(",", getUserRoles(request)),
            getClinicId(request));
    }
}
