package press.mizhifei.dentist.gateway.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import press.mizhifei.dentist.gateway.security.JwtTokenProvider;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter for API Gateway
 * Handles centralized JWT token validation and user context forwarding
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Public endpoints that don't require authentication
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/auth/",
            "/oauth2/",
            "/login/oauth2/",
            "/api/clinic/list/all",
            "/api/clinic/search",
            "/api/genai/chatbot/help",
            "/api/genai/chatbot/triage",
            "/api/genai/chatbot/receptionist",
            "/api/genai/chatbot/aidentist",
            "/api/genai/chatbot/documentation/summarize",
            "/actuator/",
            "/v3/api-docs",
            "/swagger-ui",
            "/admin");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip authentication for public endpoints - they are handled by AnonymousSessionFilter
        if (isPublicEndpoint(path)) {
            log.debug("Skipping JWT authentication for public endpoint: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || (authHeader == null || !authHeader.startsWith("Bearer "))) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return handleUnauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // Get authentication from security context (token already validated by Spring Security)
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(securityContext -> securityContext.getAuthentication())
                .cast(Authentication.class)
                .flatMap(authentication -> {
                    if (authentication != null && authentication.isAuthenticated()) {
                        // Extract user context from JWT token
                        String userId = jwtTokenProvider.getUserIdFromJWT(token);
                        String email = jwtTokenProvider.getEmailFromJWT(token);
                        String roles = jwtTokenProvider.getRolesFromJWT(token);
                        String clinicId = jwtTokenProvider.getClinicIdFromJWT(token);

                        log.debug("Authenticated user - ID: {}, Email: {}, Roles: {}, ClinicId: {}",
                                userId, email, roles, clinicId);

                        // Check role-based authorization for the endpoint
                        if (!isAuthorized(path, roles, clinicId, request)) {
                            log.warn("User {} not authorized for path: {} with roles: {}", email, path, roles);
                            return handleForbidden(exchange);
                        }

                        // Headers are already set by AnonymousSessionFilter for authenticated users
                        // Just proceed with the request
                        return chain.filter(exchange);
                    } else {
                        log.warn("Authentication failed for path: {}", path);
                        return handleUnauthorized(exchange);
                    }
                })
                .switchIfEmpty(Mono.<Void>defer(() -> {
                    log.warn("No security context found for path: {}", path);
                    return handleUnauthorized(exchange);
                }));
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Check if user is authorized to access the endpoint based on roles and clinic
     */
    private boolean isAuthorized(String path, String roles, String clinicId, ServerHttpRequest request) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        List<String> userRoles = Arrays.asList(roles.split(","));

        // System admin has access to everything
        if (userRoles.contains("SYSTEM_ADMIN")) {
            return true;
        }

        // Role-based authorization rules
        if (path.startsWith("/api/clinic/")) {
            return authorizeClinicEndpoints(path, userRoles, clinicId, request);
        } else if (path.startsWith("/api/patient/")) {
            return authorizePatientEndpoints(path, userRoles, clinicId);
        } else if (path.startsWith("/api/notification/")) {
            return authorizeNotificationEndpoints(path, userRoles, clinicId);
        } else if (path.startsWith("/api/genai/")) {
            return authorizeGenAIEndpoints(path, userRoles);
        }

        // Default: require authentication but allow access
        return true;
    }

    private boolean authorizeClinicEndpoints(String path, List<String> userRoles, String clinicId,
            ServerHttpRequest request) {
        // Clinic admin and receptionist can access clinic endpoints
        boolean hasClinicRole = userRoles.contains("CLINIC_ADMIN") || userRoles.contains("RECEPTIONIST");

        if (hasClinicRole) {
            // For clinic-specific endpoints, validate clinic access
            if (path.matches("/api/clinic/\\d+/.*")) {
                String pathClinicId = extractClinicIdFromPath(path);
                return clinicId != null && clinicId.equals(pathClinicId);
            }
            return true;
        }

        // Dentists and patients can access some clinic endpoints
        return userRoles.contains("DENTIST") || userRoles.contains("PATIENT");
    }

    private boolean authorizePatientEndpoints(String path, List<String> userRoles, String clinicId) {
        // Clinic admin, receptionist, and dentist can access patient endpoints
        return userRoles.contains("CLINIC_ADMIN") ||
                userRoles.contains("RECEPTIONIST") ||
                userRoles.contains("DENTIST") ||
                userRoles.contains("PATIENT");
    }

    private boolean authorizeNotificationEndpoints(String path, List<String> userRoles, String clinicId) {
        // All authenticated users can access notification endpoints
        return true;
    }

    private boolean authorizeGenAIEndpoints(String path, List<String> userRoles) {
        // All authenticated users can access GenAI endpoints
        return true;
    }

    private String extractClinicIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("clinic".equals(parts[i]) && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        } catch (Exception e) {
            log.error("Error extracting clinic ID from path: {}", path, e);
        }
        return null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2; // Execute after AnonymousSessionFilter
    }
}
