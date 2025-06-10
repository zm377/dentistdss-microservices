package press.mizhifei.dentist.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing anonymous user sessions across the API Gateway
 * Provides cryptographically secure session ID generation and management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
public class AnonymousSessionService {
    
    // In-memory storage for session mapping (in production, consider Redis or database)
    private final ConcurrentMap<String, SessionInfo> sessionStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates or retrieves an anonymous session ID
     * @param existingAnonId existing anonymous ID from client (if any)
     * @return SessionInfo containing session details
     */
    public SessionInfo getOrCreateSession(String existingAnonId) {
        if (StringUtils.hasText(existingAnonId) && sessionStore.containsKey(existingAnonId)) {
            SessionInfo session = sessionStore.get(existingAnonId);
            log.debug("Retrieved existing session for anonymous ID: {}", existingAnonId);
            return session;
        }

        // Generate new cryptographically secure session
        String sessionId = generateSecureSessionId();
        String anonId = existingAnonId != null ? existingAnonId : generateSecureAnonId();
        
        SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .anonymousId(anonId)
                .createdAt(System.currentTimeMillis())
                .lastAccessedAt(System.currentTimeMillis())
                .authenticated(false)
                .build();

        sessionStore.put(anonId, sessionInfo);
        log.debug("Created new anonymous session - AnonID: {}, SessionID: {}", anonId, sessionId);
        
        return sessionInfo;
    }

    /**
     * Links an anonymous session to an authenticated user
     * @param anonId anonymous ID to link
     * @param userId authenticated user ID
     * @param email user email
     * @param roles user roles
     * @param clinicId user clinic ID
     * @return updated SessionInfo
     */
    public SessionInfo linkToAuthenticatedUser(String anonId, String userId, String email, 
                                             String roles, String clinicId) {
        SessionInfo session = sessionStore.get(anonId);
        if (session == null) {
            // Create new session if not exists
            session = getOrCreateSession(anonId);
        }

        SessionInfo updatedSession = session.toBuilder()
                .authenticated(true)
                .userId(userId)
                .email(email)
                .roles(roles)
                .clinicId(clinicId)
                .lastAccessedAt(System.currentTimeMillis())
                .build();

        sessionStore.put(anonId, updatedSession);
        log.debug("Linked anonymous session {} to user {}", anonId, userId);
        
        return updatedSession;
    }

    /**
     * Updates the last accessed time for a session
     * @param anonId anonymous ID
     */
    public void updateLastAccessed(String anonId) {
        SessionInfo session = sessionStore.get(anonId);
        if (session != null) {
            SessionInfo updatedSession = session.toBuilder()
                    .lastAccessedAt(System.currentTimeMillis())
                    .build();
            sessionStore.put(anonId, updatedSession);
        }
    }

    /**
     * Generates a cryptographically secure session ID
     * @return secure UUID-based session ID
     */
    private String generateSecureSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a cryptographically secure anonymous ID
     * @return secure anonymous ID
     */
    private String generateSecureAnonId() {
        // Generate a more secure anonymous ID using SecureRandom
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
     * Gets session information by anonymous ID
     * @param anonId anonymous ID
     * @return SessionInfo or null if not found
     */
    public SessionInfo getSession(String anonId) {
        return sessionStore.get(anonId);
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
        private String anonymousId;
        private boolean authenticated;
        private String userId;
        private String email;
        private String roles;
        private String clinicId;
        private long createdAt;
        private long lastAccessedAt;
    }
}
