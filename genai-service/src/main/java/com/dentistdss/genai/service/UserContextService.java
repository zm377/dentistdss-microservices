package com.dentistdss.genai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service for extracting and managing user context from HTTP headers
 * Handles both authenticated and anonymous users
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
public class UserContextService {

    private static final String SESSION_ID_HEADER = "X-Session-ID";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String CLINIC_ID_HEADER = "X-Clinic-ID";

    /**
     * Extracts user context from HTTP request headers
     * @param request the HTTP request
     * @return UserContext object
     */
    public UserContext extractUserContext(ServerHttpRequest request) {
        String sessionId = getHeaderValue(request, SESSION_ID_HEADER);
        String userId = getHeaderValue(request, USER_ID_HEADER);
        String email = getHeaderValue(request, USER_EMAIL_HEADER);
        String rolesStr = getHeaderValue(request, USER_ROLES_HEADER);
        String clinicId = getHeaderValue(request, CLINIC_ID_HEADER);

        List<String> roles = parseRoles(rolesStr);
        boolean isAuthenticated = StringUtils.hasText(userId);

        UserContext context = UserContext.builder()
                .sessionId(sessionId)
                .userId(userId)
                .email(email)
                .roles(roles)
                .clinicId(clinicId)
                .authenticated(isAuthenticated)
                .build();

        log.debug("Extracted user context - SessionID: {}, UserID: {}, Authenticated: {}, Roles: {}", 
                sessionId, userId, isAuthenticated, roles);

        return context;
    }

    /**
     * Gets the primary role for the user (highest priority role)
     * @param context user context
     * @return primary role or "ANONYMOUS" if not authenticated
     */
    public String getPrimaryRole(UserContext context) {
        if (!context.isAuthenticated() || context.getRoles().isEmpty()) {
            return "ANONYMOUS";
        }

        // Role priority order (highest to lowest)
        List<String> rolePriority = Arrays.asList(
                "SYSTEM_ADMIN",
                "CLINIC_ADMIN", 
                "DENTIST",
                "RECEPTIONIST",
                "PATIENT"
        );

        for (String priorityRole : rolePriority) {
            if (context.getRoles().contains(priorityRole)) {
                return priorityRole;
            }
        }

        // Return first role if no priority match
        return context.getRoles().get(0);
    }

    /**
     * Checks if user has a specific role
     * @param context user context
     * @param role role to check
     * @return true if user has the role
     */
    public boolean hasRole(UserContext context, String role) {
        return context.getRoles().contains(role);
    }

    /**
     * Checks if user has any of the specified roles
     * @param context user context
     * @param roles roles to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(UserContext context, String... roles) {
        return Arrays.stream(roles).anyMatch(role -> hasRole(context, role));
    }

    /**
     * Gets user display name for personalization
     * @param context user context
     * @return display name or "Guest" for anonymous users
     */
    public String getDisplayName(UserContext context) {
        if (!context.isAuthenticated()) {
            return "Guest";
        }

        if (StringUtils.hasText(context.getEmail())) {
            // Extract name from email (before @)
            String emailName = context.getEmail().substring(0, context.getEmail().indexOf('@'));
            return capitalizeFirstLetter(emailName.replace('.', ' ').replace('_', ' '));
        }

        return "User";
    }

    private String getHeaderValue(ServerHttpRequest request, String headerName) {
        String value = request.getHeaders().getFirst(headerName);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<String> parseRoles(String rolesStr) {
        if (!StringUtils.hasText(rolesStr)) {
            return Collections.emptyList();
        }
        return Arrays.asList(rolesStr.split(","))
                .stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String capitalizeFirstLetter(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * User context holder
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserContext {
        private String sessionId;
        private String userId;
        private String email;
        private List<String> roles;
        private String clinicId;
        private boolean authenticated;
    }
}
