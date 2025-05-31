package press.mizhifei.dentist.auth.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import press.mizhifei.dentist.auth.annotation.RequireRoles;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.repository.UserRepository;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * AOP Aspect for enforcing role-based access control using @RequireRoles annotation.
 * 
 * This aspect intercepts methods annotated with @RequireRoles and validates user roles
 * before method execution. It integrates with the existing JWT authentication system.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAuthorizationAspect {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Around advice that intercepts methods annotated with @RequireRoles.
     * Validates user authentication and authorization before allowing method execution.
     * 
     * @param joinPoint the intercepted method
     * @param requireRoles the annotation containing required roles
     * @return the method result if authorized, or error response if not
     * @throws Throwable if an error occurs during method execution
     */
    @Around("@annotation(requireRoles)")
    public Object checkRoles(ProceedingJoinPoint joinPoint, RequireRoles requireRoles) throws Throwable {
        try {
            // Get the current HTTP request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.error("No request context available for role validation");
                return ResponseEntity.status(500)
                        .body(ApiResponse.error("Internal server error"));
            }

            HttpServletRequest request = attributes.getRequest();
            
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);
            if (!StringUtils.hasText(jwt)) {
                log.warn("No JWT token found in request for method: {}", joinPoint.getSignature().getName());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication token is required"));
            }

            // Validate token and extract user email
            if (!jwtTokenProvider.validateToken(jwt)) {
                log.warn("Invalid JWT token for method: {}", joinPoint.getSignature().getName());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            String userEmail = jwtTokenProvider.getEmailFromJWT(jwt);
            if (!StringUtils.hasText(userEmail)) {
                log.warn("No email found in JWT token for method: {}", joinPoint.getSignature().getName());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid authentication token"));
            }

            // Get user and their roles from database
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                log.warn("User not found with email: {} for method: {}", userEmail, joinPoint.getSignature().getName());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("User not found"));
            }

            // Check if user has any of the required roles
            Role[] requiredRoles = requireRoles.value();
            Set<Role> userRoles = user.getRoles();
            
            boolean hasRequiredRole = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);

            if (!hasRequiredRole) {
                log.warn("User {} does not have required roles {} for method: {}. User roles: {}", 
                        userEmail, Arrays.toString(requiredRoles), joinPoint.getSignature().getName(), userRoles);
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Insufficient permissions"));
            }

            log.debug("User {} authorized for method: {} with roles: {}", 
                    userEmail, joinPoint.getSignature().getName(), userRoles);

            // User is authorized, proceed with method execution
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("Error during role validation for method: {}", joinPoint.getSignature().getName(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error processing request: " + e.getMessage()));
        }
    }

    /**
     * Extracts JWT token from the Authorization header.
     * 
     * @param request the HTTP request
     * @return the JWT token without the "Bearer " prefix, or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
