package com.dentistdss.reporting.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT Utility class for extracting user information from JWT tokens
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
public class JwtUtil {
    
    /**
     * Extract user ID from Authentication object
     */
    public static Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            log.warn("Authentication is null");
            return null;
        }
        
        // Handle JWT authentication
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if (subject != null) {
                try {
                    return Long.parseLong(subject);
                } catch (NumberFormatException e) {
                    log.warn("Could not parse user ID from JWT subject: {}", subject);
                }
            }
        }
        
        // Fallback to authentication name
        String name = authentication.getName();
        if (name != null) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                log.warn("Could not parse user ID from authentication name: {}", name);
            }
        }
        
        log.warn("Could not extract user ID from authentication");
        return null;
    }
    
    /**
     * Extract user email from Authentication object
     */
    public static String extractUserEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        
        return null;
    }
    
    /**
     * Extract user roles from Authentication object
     */
    public static List<String> extractUserRoles(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String rolesString = jwt.getClaimAsString("roles");
            if (rolesString != null) {
                return Arrays.asList(rolesString.split(","));
            }
        }
        
        // Fallback to Spring Security authorities
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .toList();
    }
    
    /**
     * Extract clinic ID from Authentication object
     */
    public static Long extractClinicId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Object clinicId = jwt.getClaim("clinicId");
            if (clinicId instanceof Number) {
                return ((Number) clinicId).longValue();
            }
            if (clinicId instanceof String) {
                try {
                    return Long.parseLong((String) clinicId);
                } catch (NumberFormatException e) {
                    log.warn("Could not parse clinic ID: {}", clinicId);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if user has specific role
     */
    public static boolean hasRole(Authentication authentication, String role) {
        List<String> userRoles = extractUserRoles(authentication);
        return userRoles.contains(role);
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        List<String> userRoles = extractUserRoles(authentication);
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }
    
    /**
     * Check if user has clinic access (either system admin or belongs to the clinic)
     */
    public static boolean hasClinicAccess(Authentication authentication, Long clinicId) {
        if (hasRole(authentication, "SYSTEM_ADMIN")) {
            return true;
        }
        
        Long userClinicId = extractClinicId(authentication);
        return userClinicId != null && userClinicId.equals(clinicId);
    }
    
    /**
     * Extract JWT token from HTTP request Authorization header
     */
    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * Get user context summary for logging
     */
    public static String getUserContextSummary(Authentication authentication) {
        if (authentication == null) {
            return "User[ANONYMOUS]";
        }
        
        return String.format("User[ID=%s, Email=%s, Roles=%s, ClinicId=%s]",
            extractUserId(authentication),
            extractUserEmail(authentication),
            String.join(",", extractUserRoles(authentication)),
            extractClinicId(authentication));
    }
}
