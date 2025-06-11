package press.mizhifei.dentist.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import press.mizhifei.dentist.gateway.security.JwtTokenProvider;
import press.mizhifei.dentist.gateway.service.RateLimitService;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Rate Limiting Filter for API Gateway
 * 
 * Applies rate limiting rules to incoming requests based on configurations
 * from the system service. Supports different limits per user role, clinic,
 * and endpoint patterns.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {
    
    private final RateLimitService rateLimitService;
    private final JwtTokenProvider jwtTokenProvider;
    
    // Endpoints that should be rate limited
    private static final Set<String> RATE_LIMITED_ENDPOINTS = Set.of(
            "/api/genai/",
            "/api/reports/",
            "/api/chatlogs/"
    );
    
    // Public endpoints that don't require rate limiting
    private static final Set<String> EXCLUDED_ENDPOINTS = Set.of(
            "/api/auth/",
            "/oauth2/",
            "/login/oauth2/",
            "/actuator/",
            "/v3/api-docs",
            "/swagger-ui",
            "/admin"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        
        // Skip rate limiting for excluded endpoints
        if (shouldSkipRateLimit(path)) {
            log.debug("Skipping rate limit for path: {}", path);
            return chain.filter(exchange);
        }
        
        // Only apply rate limiting to specific endpoints
        if (!shouldApplyRateLimit(path)) {
            return chain.filter(exchange);
        }
        
        // Extract user context
        String sessionId = extractSessionId(request);
        String userRole = extractUserRole(request);
        Long clinicId = extractClinicId(request);
        
        // Estimate token consumption for the request
        long tokens = rateLimitService.estimateTokens(path, ""); // Content estimation would require body reading

        log.debug("Checking rate limit for path: {}, session: {}, role: {}, clinic: {}, tokens: {}",
                path, sessionId, userRole, clinicId, tokens);
        
        // Check rate limit
        return rateLimitService.isAllowed(path, sessionId, userRole, clinicId, tokens)
                .flatMap(allowed -> {
                    if (allowed) {
                        log.debug("Rate limit check passed for session: {}", sessionId);
                        return chain.filter(exchange);
                    } else {
                        log.warn("Rate limit exceeded for session: {} on path: {}", sessionId, path);
                        return handleRateLimitExceeded(exchange);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error in rate limit filter, allowing request: {}", error.getMessage(), error);
                    return chain.filter(exchange); // Allow on error to prevent service disruption
                });
    }
    
    /**
     * Check if rate limiting should be skipped for this path
     */
    private boolean shouldSkipRateLimit(String path) {
        return EXCLUDED_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Check if rate limiting should be applied to this path
     */
    private boolean shouldApplyRateLimit(String path) {
        return RATE_LIMITED_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Extract session ID from request headers
     */
    private String extractSessionId(ServerHttpRequest request) {
        String sessionId = request.getHeaders().getFirst("X-Session-ID");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // Fallback to remote address if no session ID
            sessionId = request.getRemoteAddress() != null ? 
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return sessionId;
    }
    
    /**
     * Extract user role from JWT token
     */
    private String extractUserRole(ServerHttpRequest request) {
        try {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String roles = jwtTokenProvider.getRolesFromJWT(token);
                
                // Return the first role (you might want to implement priority logic)
                if (roles != null && !roles.trim().isEmpty()) {
                    return roles.split(",")[0].trim();
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract user role from token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Extract clinic ID from JWT token
     */
    private Long extractClinicId(ServerHttpRequest request) {
        try {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String clinicIdStr = jwtTokenProvider.getClinicIdFromJWT(token);
                
                if (clinicIdStr != null && !clinicIdStr.trim().isEmpty()) {
                    return Long.parseLong(clinicIdStr);
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract clinic ID from token: {}", e.getMessage());
        }
        return null;
    }
    

    
    /**
     * Handle rate limit exceeded response
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-RateLimit-Exceeded", "true");
        
        String errorMessage = """
                {
                    "error": "Rate limit exceeded",
                    "message": "You have exceeded the rate limit for this service. Please try again later.",
                    "status": 429,
                    "timestamp": "%s"
                }
                """.formatted(java.time.Instant.now().toString());
        
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3; // Execute after JWT and session filters
    }
}
