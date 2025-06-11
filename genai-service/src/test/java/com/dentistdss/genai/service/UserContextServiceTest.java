package com.dentistdss.genai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserContextService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
class UserContextServiceTest {

    private UserContextService userContextService;

    @BeforeEach
    void setUp() {
        userContextService = new UserContextService();
    }

    @Test
    void testExtractUserContext_AuthenticatedUser() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Session-ID", "session-123")
                .header("X-User-ID", "user-456")
                .header("X-User-Email", "john.doe@example.com")
                .header("X-User-Roles", "DENTIST,CLINIC_ADMIN")
                .header("X-Clinic-ID", "clinic-789")
                .build();

        // When
        UserContextService.UserContext context = userContextService.extractUserContext(request);

        // Then
        assertNotNull(context);
        assertEquals("session-123", context.getSessionId());
        assertEquals("user-456", context.getUserId());
        assertEquals("john.doe@example.com", context.getEmail());
        assertEquals("clinic-789", context.getClinicId());
        assertTrue(context.isAuthenticated());
        assertEquals(2, context.getRoles().size());
        assertTrue(context.getRoles().contains("DENTIST"));
        assertTrue(context.getRoles().contains("CLINIC_ADMIN"));
    }

    @Test
    void testExtractUserContext_AnonymousUser() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Session-ID", "session-123")
                .build();

        // When
        UserContextService.UserContext context = userContextService.extractUserContext(request);

        // Then
        assertNotNull(context);
        assertEquals("session-123", context.getSessionId());
        assertNull(context.getUserId());
        assertNull(context.getEmail());
        assertNull(context.getClinicId());
        assertFalse(context.isAuthenticated());
        assertTrue(context.getRoles().isEmpty());
    }

    @Test
    void testGetPrimaryRole_AuthenticatedUser() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .authenticated(true)
                .roles(java.util.Arrays.asList("PATIENT", "DENTIST"))
                .build();

        // When
        String primaryRole = userContextService.getPrimaryRole(context);

        // Then
        assertEquals("DENTIST", primaryRole); // DENTIST has higher priority than PATIENT
    }

    @Test
    void testGetPrimaryRole_AnonymousUser() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .authenticated(false)
                .roles(java.util.Collections.emptyList())
                .build();

        // When
        String primaryRole = userContextService.getPrimaryRole(context);

        // Then
        assertEquals("ANONYMOUS", primaryRole);
    }

    @Test
    void testHasRole() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .roles(java.util.Arrays.asList("DENTIST", "CLINIC_ADMIN"))
                .build();

        // When & Then
        assertTrue(userContextService.hasRole(context, "DENTIST"));
        assertTrue(userContextService.hasRole(context, "CLINIC_ADMIN"));
        assertFalse(userContextService.hasRole(context, "PATIENT"));
    }

    @Test
    void testHasAnyRole() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .roles(java.util.Arrays.asList("DENTIST"))
                .build();

        // When & Then
        assertTrue(userContextService.hasAnyRole(context, "DENTIST", "PATIENT"));
        assertTrue(userContextService.hasAnyRole(context, "PATIENT", "DENTIST"));
        assertFalse(userContextService.hasAnyRole(context, "PATIENT", "RECEPTIONIST"));
    }

    @Test
    void testGetDisplayName_AuthenticatedUser() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .authenticated(true)
                .email("john.doe@example.com")
                .build();

        // When
        String displayName = userContextService.getDisplayName(context);

        // Then
        assertEquals("John doe", displayName);
    }

    @Test
    void testGetDisplayName_AnonymousUser() {
        // Given
        UserContextService.UserContext context = UserContextService.UserContext.builder()
                .authenticated(false)
                .build();

        // When
        String displayName = userContextService.getDisplayName(context);

        // Then
        assertEquals("Guest", displayName);
    }
}
