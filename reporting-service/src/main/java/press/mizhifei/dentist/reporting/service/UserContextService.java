package press.mizhifei.dentist.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * User Context Service
 * 
 * Retrieves user information and context from other microservices.
 * Implements caching for performance optimization.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserProfileClient userProfileClient;

    /**
     * Get user context with caching
     */
    @Cacheable(value = "userPermissions", key = "#userId", cacheManager = "caffeineCacheManager")
    public SecurityService.UserContext getUserContext(Long userId) {
        try {
            UserProfileResponse userProfile = userProfileClient.getUserProfile(userId);
            
            return new SecurityService.UserContext(
                userProfile.getId(),
                userProfile.getClinicId(),
                userProfile.getRoles(),
                userProfile.getEmail()
            );
            
        } catch (Exception e) {
            log.error("Error retrieving user context for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Feign client for user profile service
     */
    @FeignClient(name = "user-profile-service", path = "/users")
    public interface UserProfileClient {
        
        @GetMapping("/{userId}")
        UserProfileResponse getUserProfile(@PathVariable Long userId);
    }

    /**
     * User profile response DTO
     */
    public static class UserProfileResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Long clinicId;
        private List<String> roles;
        private Boolean active;

        // Constructors
        public UserProfileResponse() {}

        public UserProfileResponse(Long id, String email, String firstName, String lastName, 
                                 Long clinicId, List<String> roles, Boolean active) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.clinicId = clinicId;
            this.roles = roles;
            this.active = active;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public Long getClinicId() { return clinicId; }
        public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}
