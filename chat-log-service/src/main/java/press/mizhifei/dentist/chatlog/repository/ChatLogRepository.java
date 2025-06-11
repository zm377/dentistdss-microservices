package press.mizhifei.dentist.chatlog.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.chatlog.model.ChatLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reactive MongoDB repository for ChatLog entities
 * Provides comprehensive querying capabilities for chat log data
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ChatLogRepository extends ReactiveMongoRepository<ChatLog, String> {
    
    /**
     * Find all chat logs for a specific session, ordered by message sequence
     */
    Flux<ChatLog> findBySessionIdOrderByMessageSequenceAsc(UUID sessionId);
    
    /**
     * Find all chat logs for a specific user, ordered by timestamp descending
     */
    Flux<ChatLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    /**
     * Find all chat logs for a specific clinic, ordered by timestamp descending
     */
    Flux<ChatLog> findByClinicIdOrderByTimestampDesc(Long clinicId, Pageable pageable);
    
    /**
     * Find chat logs by user and date range
     */
    Flux<ChatLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find chat logs by clinic and date range
     */
    Flux<ChatLog> findByClinicIdAndTimestampBetweenOrderByTimestampDesc(
            Long clinicId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Count total chat logs for a user
     */
    Mono<Long> countByUserId(Long userId);
    
    /**
     * Count total chat logs for a clinic
     */
    Mono<Long> countByClinicId(Long clinicId);
    
    /**
     * Count chat logs in a session
     */
    Mono<Long> countBySessionId(UUID sessionId);
    
    /**
     * Find chat logs with PHI redaction
     */
    @Query("{ 'phiRedaction.phiDetected': true }")
    Flux<ChatLog> findLogsWithPHIRedaction(Pageable pageable);
    
    /**
     * Full-text search in user messages and AI responses
     */
    @Query("{ $or: [ " +
           "{ 'userMessage': { $regex: ?0, $options: 'i' } }, " +
           "{ 'aiResponse': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Flux<ChatLog> searchByContent(String searchTerm, Pageable pageable);
    
    /**
     * Search within a specific clinic
     */
    @Query("{ 'clinicId': ?0, $or: [ " +
           "{ 'userMessage': { $regex: ?1, $options: 'i' } }, " +
           "{ 'aiResponse': { $regex: ?1, $options: 'i' } } " +
           "] }")
    Flux<ChatLog> searchByContentAndClinic(Long clinicId, String searchTerm, Pageable pageable);
    
    /**
     * Find recent chat logs for a user (last 24 hours)
     */
    @Query("{ 'userId': ?0, 'timestamp': { $gte: ?1 } }")
    Flux<ChatLog> findRecentByUserId(Long userId, LocalDateTime since);
}
