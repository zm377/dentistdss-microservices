package com.dentistdss.systemadmin.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Optional;

/**
 * Security Utilities for System Administration Service
 * 
 * Provides helper methods for security-related operations
 * and user context extraction
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
public class SecurityUtils {

    /**
     * Get current authenticated user's username
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        
        return Optional.ofNullable(authentication.getName());
    }

    /**
     * Get current authenticated user's email
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        
        return Optional.empty();
    }

    /**
     * Get current authenticated user's roles
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Collections.emptyList();
        }
        
        return authentication.getAuthorities();
    }

    /**
     * Check if current user has SYSTEM_ADMIN role
     */
    public static boolean isSystemAdmin() {
        return hasRole("SYSTEM_ADMIN");
    }

    /**
     * Check if current user has CLINIC_ADMIN role
     */
    public static boolean isClinicAdmin() {
        return hasRole("CLINIC_ADMIN");
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserRoles();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Get current user's clinic ID (if applicable)
     */
    public static Optional<Long> getCurrentUserClinicId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Object clinicId = jwt.getClaim("clinic_id");
            if (clinicId instanceof Number) {
                return Optional.of(((Number) clinicId).longValue());
            }
            if (clinicId instanceof String) {
                try {
                    return Optional.of(Long.parseLong((String) clinicId));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Get current authentication object
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Get JWT token claims
     */
    public static Optional<Jwt> getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        
        return Optional.empty();
    }

    /**
     * Get specific claim from JWT token
     */
    public static Optional<Object> getJwtClaim(String claimName) {
        return getCurrentJwtToken()
                .map(jwt -> jwt.getClaim(claimName));
    }

    /**
     * Get string claim from JWT token
     */
    public static Optional<String> getJwtStringClaim(String claimName) {
        return getCurrentJwtToken()
                .map(jwt -> jwt.getClaimAsString(claimName));
    }

    /**
     * Validate that current user has required role for system administration
     */
    public static void requireSystemAdminRole() {
        if (!isSystemAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "SYSTEM_ADMIN role required for this operation");
        }
    }

    /**
     * Get user identifier for audit logging
     */
    public static String getUserIdentifierForAudit() {
        return getCurrentUsername()
                .or(() -> getCurrentUserEmail())
                .orElse("UNKNOWN_USER");
    }
}
