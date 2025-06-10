package press.mizhifei.dentist.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import press.mizhifei.dentist.gateway.security.JwtTokenProvider;
import press.mizhifei.dentist.gateway.service.AnonymousSessionService;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Filter for managing user sessions and header propagation
 * Handles both anonymous and authenticated users, ensuring proper session tracking
 * Uses a single sessionId for both anonymous and authenticated users
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnonymousSessionFilter implements GlobalFilter, Ordered {

    private final AnonymousSessionService anonymousSessionService;
    private final JwtTokenProvider jwtTokenProvider;

    // Public endpoints that should receive session tracking
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/genai/chatbot/help",
            "/api/genai/chatbot/triage",
            "/api/genai/chatbot/receptionist",
            "/api/genai/chatbot/aidentist",
            "/api/genai/chatbot/documentation/summarize",
            "/api/clinic/list/all",
            "/api/clinic/search"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip session management for non-relevant endpoints
        if (shouldSkipSessionManagement(path)) {
            return chain.filter(exchange);
        }

        // Get existing session ID from request headers
        String existingSessionId = request.getHeaders().getFirst("X-Session-ID");

        // Check if user is authenticated
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(securityContext -> securityContext.getAuthentication())
                .cast(Authentication.class)
                .flatMap(authentication -> {
                    if (authentication != null && authentication.isAuthenticated()) {
                        // Handle authenticated user
                        return handleAuthenticatedUser(exchange, chain, existingSessionId, authentication);
                    } else {
                        // Handle anonymous user
                        return handleAnonymousUser(exchange, chain, existingSessionId);
                    }
                })
                .switchIfEmpty(Mono.<Void>defer(() -> {
                    // No security context - handle as anonymous user
                    return handleAnonymousUser(exchange, chain, existingSessionId);
                }));
    }

    /**
     * Handles authenticated users by linking their session to their user account
     */
    private Mono<Void> handleAuthenticatedUser(ServerWebExchange exchange, GatewayFilterChain chain,
                                             String existingSessionId, Authentication authentication) {
        ServerHttpRequest request = exchange.getRequest();

        // Extract JWT token and user information
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            String userId = jwtTokenProvider.getUserIdFromJWT(token);
            String email = jwtTokenProvider.getEmailFromJWT(token);
            String roles = jwtTokenProvider.getRolesFromJWT(token);
            String clinicId = jwtTokenProvider.getClinicIdFromJWT(token);

            // Link session to authenticated user
            AnonymousSessionService.SessionInfo sessionInfo =
                    anonymousSessionService.linkToAuthenticatedUser(
                            existingSessionId, userId, email, roles, clinicId);

            // Add headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Session-ID", sessionInfo.getSessionId())
                    .header("X-User-ID", userId != null ? userId : "")
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Roles", roles != null ? roles : "")
                    .header("X-Clinic-ID", clinicId != null ? clinicId : "")
                    .build();

            log.debug("Authenticated user session - SessionID: {}, UserID: {}",
                    sessionInfo.getSessionId(), userId);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // Fallback to anonymous handling if token extraction fails
        return handleAnonymousUser(exchange, chain, existingSessionId);
    }

    /**
     * Handles anonymous users by creating or retrieving their session
     */
    private Mono<Void> handleAnonymousUser(ServerWebExchange exchange, GatewayFilterChain chain,
                                         String existingSessionId) {
        // Get or create session
        AnonymousSessionService.SessionInfo sessionInfo =
                anonymousSessionService.getOrCreateSession(existingSessionId);

        // Add headers for downstream services
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Session-ID", sessionInfo.getSessionId())
                .header("X-User-ID", "") // Empty for anonymous users
                .header("X-User-Email", "") // Empty for anonymous users
                .header("X-User-Roles", "") // Empty for anonymous users
                .header("X-Clinic-ID", "") // Empty for anonymous users
                .build();

        log.debug("Anonymous user session - SessionID: {}", sessionInfo.getSessionId());

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Determines if session management should be skipped for this path
     */
    private boolean shouldSkipSessionManagement(String path) {
        // Skip for auth endpoints, actuator, swagger, etc.
        return path.startsWith("/api/auth/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/oauth2/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/admin");
    }

    /**
     * Checks if the endpoint is public and should receive session tracking
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Execute before JWT authentication filter
    }
}
