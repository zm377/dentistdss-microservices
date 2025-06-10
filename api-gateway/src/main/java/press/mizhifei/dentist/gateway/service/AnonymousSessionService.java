package press.mizhifei.dentist.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing user sessions across the API Gateway
 * Provides cryptographically secure session ID generation and management
 * Uses a single sessionId for both anonymous and authenticated users
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
public class AnonymousSessionService {

    private static final String SESSION_ID_HEADER = "X-Session-ID";

    // In-memory storage for session mapping (in production, consider Redis or database)
    private final ConcurrentMap<String, SessionInfo> sessionStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates or retrieves a session
     * @param existingSessionId existing session ID from client (if any)
     * @return SessionInfo containing session details
     */
    public SessionInfo getOrCreateSession(String existingSessionId) {
        if (StringUtils.hasText(existingSessionId) && sessionStore.containsKey(existingSessionId)) {
            SessionInfo session = sessionStore.get(existingSessionId);
            log.debug("Retrieved existing session: {}", existingSessionId);
            return session;
        }

        // Generate new cryptographically secure session
        String sessionId = existingSessionId != null ? existingSessionId : generateSecureSessionId();

        SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .createdAt(System.currentTimeMillis())
                .lastAccessedAt(System.currentTimeMillis())
                .authenticated(false)
                .build();

        sessionStore.put(sessionId, sessionInfo);
        log.debug("Created new session: {}", sessionId);

        return sessionInfo;
    }

    /**
     * Links a session to an authenticated user
     * @param sessionId session ID to link
     * @param userId authenticated user ID
     * @param email user email
     * @param roles user roles
     * @param clinicId user clinic ID
     * @return updated SessionInfo
     */
    public SessionInfo linkToAuthenticatedUser(String sessionId, String userId, String email,
                                             String roles, String clinicId) {
        SessionInfo session = sessionStore.get(sessionId);
        if (session == null) {
            // Create new session if not exists
            session = getOrCreateSession(sessionId);
        }

        SessionInfo updatedSession = session.toBuilder()
                .authenticated(true)
                .userId(userId)
                .email(email)
                .roles(roles)
                .clinicId(clinicId)
                .lastAccessedAt(System.currentTimeMillis())
                .build();

        sessionStore.put(sessionId, updatedSession);
        log.debug("Linked session {} to user {}", sessionId, userId);

        return updatedSession;
    }

    /**
     * Updates the last accessed time for a session
     * @param sessionId session ID
     */
    public void updateLastAccessed(String sessionId) {
        SessionInfo session = sessionStore.get(sessionId);
        if (session != null) {
            SessionInfo updatedSession = session.toBuilder()
                    .lastAccessedAt(System.currentTimeMillis())
                    .build();
            sessionStore.put(sessionId, updatedSession);
        }
    }

    /**
     * Generates a cryptographically secure session ID
     * @return secure UUID-based session ID
     */
    private String generateSecureSessionId() {
        // Generate a more secure session ID using SecureRandom
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // Convert to UUID format for consistency
        return UUID.nameUUIDFromBytes(randomBytes).toString();
    }

    /**
     * Cleans up expired sessions (should be called periodically)
     * @param maxAgeMs maximum age in milliseconds
     */
    public void cleanupExpiredSessions(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        sessionStore.entrySet().removeIf(entry -> {
            SessionInfo session = entry.getValue();
            boolean expired = (currentTime - session.getLastAccessedAt()) > maxAgeMs;
            if (expired) {
                log.debug("Removing expired session: {}", entry.getKey());
            }
            return expired;
        });
    }

    /**
     * Gets session information by session ID
     * @param sessionId session ID
     * @return SessionInfo or null if not found
     */
    public SessionInfo getSession(String sessionId) {
        return sessionStore.get(sessionId);
    }

    /**
     * Session information holder
     */
    @lombok.Data
    @lombok.Builder(toBuilder = true)
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SessionInfo {
        private String sessionId;
        private boolean authenticated;
        private String userId;
        private String email;
        private String roles;
        private String clinicId;
        private long createdAt;
        private long lastAccessedAt;
    }
}
