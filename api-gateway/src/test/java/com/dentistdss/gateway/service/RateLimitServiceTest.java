package com.dentistdss.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dentistdss.gateway.dto.RateLimitConfigDto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RateLimitConfigResolver configResolver;
    
    @Mock
    private BucketManager bucketManager;
    
    @Mock
    private TokenEstimator tokenEstimator;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(configResolver, bucketManager, tokenEstimator);
    }

    @Test
    void testIsAllowed_WithValidConfig_ShouldReturnTrue() {
        // Given
        String endpoint = "/api/genai/help";
        String sessionId = "session123";
        String userRole = "DENTIST";
        Long clinicId = 1L;
        long tokens = 100;

        RateLimitConfigDto config = RateLimitConfigDto.builder()
                .configName("genai-dentist")
                .maxRequests(1000L)
                .timeWindowSeconds(180L)
                .limitType(RateLimitConfigDto.RateLimitType.TOKEN_COUNT)
                .active(true)
                .build();

        when(configResolver.resolveConfig(endpoint, userRole, clinicId)).thenReturn(config);
        when(bucketManager.getBucket(eq(config), eq(sessionId))).thenReturn(
                io.github.bucket4j.Bucket.builder()
                        .addLimit(io.github.bucket4j.Bandwidth.classic(1000, 
                                io.github.bucket4j.Refill.greedy(1000, java.time.Duration.ofMinutes(3))))
                        .build()
        );

        // When & Then
        StepVerifier.create(rateLimitService.isAllowed(endpoint, sessionId, userRole, clinicId, tokens))
                .expectNext(true)
                .verifyComplete();

        verify(configResolver).resolveConfig(endpoint, userRole, clinicId);
        verify(bucketManager).getBucket(config, sessionId);
    }

    @Test
    void testIsAllowed_WithExceededLimit_ShouldReturnFalse() {
        // Given
        String endpoint = "/api/genai/help";
        String sessionId = "session123";
        String userRole = "DENTIST";
        Long clinicId = 1L;
        long tokens = 2000; // Exceeds limit

        RateLimitConfigDto config = RateLimitConfigDto.builder()
                .configName("genai-dentist")
                .maxRequests(1000L)
                .timeWindowSeconds(180L)
                .limitType(RateLimitConfigDto.RateLimitType.TOKEN_COUNT)
                .active(true)
                .build();

        // Create a bucket that's already exhausted
        io.github.bucket4j.Bucket exhaustedBucket = io.github.bucket4j.Bucket.builder()
                .addLimit(io.github.bucket4j.Bandwidth.classic(1000, 
                        io.github.bucket4j.Refill.greedy(1000, java.time.Duration.ofMinutes(3))))
                .build();
        
        // Consume all tokens
        exhaustedBucket.tryConsume(1000);

        when(configResolver.resolveConfig(endpoint, userRole, clinicId)).thenReturn(config);
        when(bucketManager.getBucket(eq(config), eq(sessionId))).thenReturn(exhaustedBucket);

        // When & Then
        StepVerifier.create(rateLimitService.isAllowed(endpoint, sessionId, userRole, clinicId, tokens))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testIsAllowed_WithError_ShouldReturnTrue() {
        // Given
        String endpoint = "/api/genai/help";
        String sessionId = "session123";
        String userRole = "DENTIST";
        Long clinicId = 1L;
        long tokens = 100;

        when(configResolver.resolveConfig(endpoint, userRole, clinicId))
                .thenThrow(new RuntimeException("Config service unavailable"));

        // When & Then
        StepVerifier.create(rateLimitService.isAllowed(endpoint, sessionId, userRole, clinicId, tokens))
                .expectNext(true) // Should allow on error
                .verifyComplete();
    }

    @Test
    void testEstimateTokens() {
        // Given
        String endpoint = "/api/genai/help";
        String content = "test content";
        long expectedTokens = 50;

        when(tokenEstimator.estimateTokens(endpoint, content)).thenReturn(expectedTokens);

        // When
        long actualTokens = rateLimitService.estimateTokens(endpoint, content);

        // Then
        assert actualTokens == expectedTokens;
        verify(tokenEstimator).estimateTokens(endpoint, content);
    }

    @Test
    void testClearCache() {
        // When
        rateLimitService.clearCache();

        // Then
        verify(bucketManager).clearAllBuckets();
    }
}
