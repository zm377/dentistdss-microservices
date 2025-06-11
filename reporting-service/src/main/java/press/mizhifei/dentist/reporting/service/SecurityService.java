package press.mizhifei.dentist.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.reporting.model.ReportTemplate;
import press.mizhifei.dentist.reporting.repository.ReportTemplateRepository;

import java.util.List;
import java.util.Optional;

/**
 * Security Service for Reporting
 * 
 * Handles authorization and access control for report templates and executions.
 * Implements role-based security with clinic-level data isolation.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final ReportTemplateRepository templateRepository;
    private final UserContextService userContextService;

    /**
     * Check if user can access a specific template
     */
    @Cacheable(value = "userPermissions", key = "#templateCode + '_' + #userId", 
               cacheManager = "caffeineCacheManager")
    public boolean canAccessTemplate(String templateCode, Long userId) {
        try {
            // Get user context
            UserContext userContext = userContextService.getUserContext(userId);
            if (userContext == null) {
                log.warn("User context not found for user: {}", userId);
                return false;
            }
            
            // Get template
            Optional<ReportTemplate> templateOpt = templateRepository.findByTemplateCodeAndActive(templateCode, true);
            if (templateOpt.isEmpty()) {
                log.warn("Template not found or inactive: {}", templateCode);
                return false;
            }
            
            ReportTemplate template = templateOpt.get();
            
            // Check role-based access
            if (template.getAllowedRoles() == null || template.getAllowedRoles().isEmpty()) {
                // If no roles specified, allow all authenticated users
                return true;
            }
            
            // Check if user has any of the required roles
            for (String userRole : userContext.getRoles()) {
                if (template.getAllowedRoles().contains(userRole)) {
                    return true;
                }
            }
            
            log.debug("User {} does not have required roles for template {}", userId, templateCode);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking template access for user {} and template {}: {}", 
                    userId, templateCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if user can access clinic data
     */
    public boolean canAccessClinicData(Long userId, Long clinicId) {
        try {
            UserContext userContext = userContextService.getUserContext(userId);
            if (userContext == null) {
                return false;
            }
            
            // System admins can access all clinic data
            if (userContext.getRoles().contains("SYSTEM_ADMIN")) {
                return true;
            }
            
            // Users can only access their own clinic data
            return userContext.getClinicId() != null && userContext.getClinicId().equals(clinicId);
            
        } catch (Exception e) {
            log.error("Error checking clinic access for user {} and clinic {}: {}", 
                    userId, clinicId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get accessible clinic IDs for user
     */
    public List<Long> getAccessibleClinicIds(Long userId) {
        try {
            UserContext userContext = userContextService.getUserContext(userId);
            if (userContext == null) {
                return List.of();
            }
            
            // System admins can access all clinics (return empty list to indicate "all")
            if (userContext.getRoles().contains("SYSTEM_ADMIN")) {
                return List.of(); // Empty list means all clinics
            }
            
            // Regular users can only access their clinic
            if (userContext.getClinicId() != null) {
                return List.of(userContext.getClinicId());
            }
            
            return List.of();
            
        } catch (Exception e) {
            log.error("Error getting accessible clinics for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Check if user can schedule reports
     */
    public boolean canScheduleReports(Long userId) {
        try {
            UserContext userContext = userContextService.getUserContext(userId);
            if (userContext == null) {
                return false;
            }
            
            // Only admins and clinic admins can schedule reports
            return userContext.getRoles().contains("SYSTEM_ADMIN") ||
                   userContext.getRoles().contains("CLINIC_ADMIN");
                   
        } catch (Exception e) {
            log.error("Error checking schedule permission for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Apply data anonymization based on user role
     */
    public boolean shouldAnonymizeData(Long userId, String templateCode) {
        try {
            UserContext userContext = userContextService.getUserContext(userId);
            if (userContext == null) {
                return true; // Default to anonymized
            }
            
            // System admins and clinic admins can see non-anonymized data
            if (userContext.getRoles().contains("SYSTEM_ADMIN") ||
                userContext.getRoles().contains("CLINIC_ADMIN")) {
                return false;
            }
            
            // Other roles get anonymized data by default
            return true;
            
        } catch (Exception e) {
            log.error("Error determining anonymization for user {}: {}", userId, e.getMessage(), e);
            return true; // Default to anonymized on error
        }
    }

    /**
     * User context holder
     */
    public static class UserContext {
        private final Long userId;
        private final Long clinicId;
        private final List<String> roles;
        private final String email;

        public UserContext(Long userId, Long clinicId, List<String> roles, String email) {
            this.userId = userId;
            this.clinicId = clinicId;
            this.roles = roles;
            this.email = email;
        }

        public Long getUserId() { return userId; }
        public Long getClinicId() { return clinicId; }
        public List<String> getRoles() { return roles; }
        public String getEmail() { return email; }
    }
}
