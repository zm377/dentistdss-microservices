package com.dentistdss.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Token Estimator for Rate Limiting
 * 
 * Estimates token consumption for different types of requests.
 * Follows Single Responsibility Principle by focusing only on token estimation.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
public class TokenEstimator {
    
    // Constants for token estimation
    private static final int CHARACTERS_PER_TOKEN = 4;
    private static final long MIN_TOKEN_COUNT = 10;
    private static final long DEFAULT_GENAI_TOKENS = 100;
    private static final long DEFAULT_REQUEST_TOKENS = 1;
    
    /**
     * Estimate token consumption for a request
     */
    public long estimateTokens(String endpoint, String content) {
        if (isGenAIEndpoint(endpoint)) {
            return estimateGenAITokens(content);
        } else {
            return DEFAULT_REQUEST_TOKENS;
        }
    }
    
    /**
     * Estimate tokens for GenAI requests
     */
    private long estimateGenAITokens(String content) {
        if (content == null || content.trim().isEmpty()) {
            return MIN_TOKEN_COUNT;
        }
        
        // Simple estimation: ~4 characters per token (rough approximation)
        // In production, you might want to use a more sophisticated tokenizer
        long estimatedTokens = Math.max(MIN_TOKEN_COUNT, content.length() / CHARACTERS_PER_TOKEN);
        
        log.debug("Estimated {} tokens for content length: {}", estimatedTokens, content.length());
        return estimatedTokens;
    }
    
    /**
     * Check if endpoint is a GenAI endpoint
     */
    private boolean isGenAIEndpoint(String endpoint) {
        return endpoint != null && endpoint.startsWith("/api/genai/");
    }
    
    /**
     * Get default token estimate for GenAI requests when content is not available
     */
    public long getDefaultGenAITokens() {
        return DEFAULT_GENAI_TOKENS;
    }
}
