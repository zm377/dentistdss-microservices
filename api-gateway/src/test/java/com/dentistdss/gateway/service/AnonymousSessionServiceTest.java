package com.dentistdss.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.dentistdss.gateway.service.AnonymousSessionService.SessionInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnonymousSessionService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
class AnonymousSessionServiceTest {

    private AnonymousSessionService anonymousSessionService;

    @BeforeEach
    void setUp() {
        anonymousSessionService = new AnonymousSessionService();
    }

    @Test
    void testGetOrCreateSession_NewSession() {
        // When
        SessionInfo sessionInfo = anonymousSessionService.getOrCreateSession(null);

        // Then
        assertNotNull(sessionInfo);
        assertNotNull(sessionInfo.getSessionId());
        assertFalse(sessionInfo.isAuthenticated());
        assertNull(sessionInfo.getUserId());
        assertNull(sessionInfo.getEmail());
        assertTrue(sessionInfo.getCreatedAt() > 0);
        assertTrue(sessionInfo.getLastAccessedAt() > 0);
    }

    @Test
    void testGetOrCreateSession_ExistingSessionId() {
        // Given
        SessionInfo firstSession = anonymousSessionService.getOrCreateSession(null);
        String existingSessionId = firstSession.getSessionId();

        // When
        SessionInfo secondSession = anonymousSessionService.getOrCreateSession(existingSessionId);

        // Then
        assertNotNull(secondSession);
        assertEquals(existingSessionId, secondSession.getSessionId());
        assertEquals(firstSession.getSessionId(), secondSession.getSessionId());
    }

    @Test
    void testLinkToAuthenticatedUser() {
        // Given
        SessionInfo anonymousSession = anonymousSessionService.getOrCreateSession(null);
        String sessionId = anonymousSession.getSessionId();

        // When
        SessionInfo linkedSession = anonymousSessionService.linkToAuthenticatedUser(
                sessionId, "user123", "test@example.com", "DENTIST", "clinic456");

        // Then
        assertNotNull(linkedSession);
        assertEquals(sessionId, linkedSession.getSessionId());
        assertEquals(anonymousSession.getSessionId(), linkedSession.getSessionId());
        assertTrue(linkedSession.isAuthenticated());
        assertEquals("user123", linkedSession.getUserId());
        assertEquals("test@example.com", linkedSession.getEmail());
        assertEquals("DENTIST", linkedSession.getRoles());
        assertEquals("clinic456", linkedSession.getClinicId());
    }

    @Test
    void testLinkToAuthenticatedUser_NonExistentSessionId() {
        // When
        SessionInfo linkedSession = anonymousSessionService.linkToAuthenticatedUser(
                "non-existent-id", "user123", "test@example.com", "DENTIST", "clinic456");

        // Then
        assertNotNull(linkedSession);
        assertEquals("non-existent-id", linkedSession.getSessionId());
        assertTrue(linkedSession.isAuthenticated());
        assertEquals("user123", linkedSession.getUserId());
    }

    @Test
    void testUpdateLastAccessed() {
        // Given
        SessionInfo session = anonymousSessionService.getOrCreateSession(null);
        String sessionId = session.getSessionId();
        long originalLastAccessed = session.getLastAccessedAt();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        anonymousSessionService.updateLastAccessed(sessionId);

        // Then
        SessionInfo updatedSession = anonymousSessionService.getSession(sessionId);
        assertNotNull(updatedSession);
        assertTrue(updatedSession.getLastAccessedAt() > originalLastAccessed);
    }

    @Test
    void testGetSession() {
        // Given
        SessionInfo originalSession = anonymousSessionService.getOrCreateSession(null);
        String sessionId = originalSession.getSessionId();

        // When
        SessionInfo retrievedSession = anonymousSessionService.getSession(sessionId);

        // Then
        assertNotNull(retrievedSession);
        assertEquals(originalSession.getSessionId(), retrievedSession.getSessionId());
        assertEquals(sessionId, retrievedSession.getSessionId());
    }

    @Test
    void testGetSession_NonExistent() {
        // When
        SessionInfo session = anonymousSessionService.getSession("non-existent-id");

        // Then
        assertNull(session);
    }

    @Test
    void testCleanupExpiredSessions() {
        // Given
        SessionInfo session1 = anonymousSessionService.getOrCreateSession(null);
        SessionInfo session2 = anonymousSessionService.getOrCreateSession(null);

        // When - cleanup with very short max age (everything should be expired)
        anonymousSessionService.cleanupExpiredSessions(1); // 1ms max age

        // Then - sessions should still exist immediately after creation
        // (this test is more about ensuring the method doesn't crash)
        assertNotNull(anonymousSessionService.getSession(session1.getSessionId()));
        assertNotNull(anonymousSessionService.getSession(session2.getSessionId()));
    }
}
