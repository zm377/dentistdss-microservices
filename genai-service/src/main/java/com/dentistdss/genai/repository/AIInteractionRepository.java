package com.dentistdss.genai.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.dentistdss.genai.model.AIInteraction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface AIInteractionRepository extends ReactiveMongoRepository<AIInteraction, String> {
    
    Flux<AIInteraction> findByUserId(Long userId);
    
    Flux<AIInteraction> findBySessionId(UUID sessionId);
    
    Mono<Long> countBySessionId(UUID sessionId);
} 