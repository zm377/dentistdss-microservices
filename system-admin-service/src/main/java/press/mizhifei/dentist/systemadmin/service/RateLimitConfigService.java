package press.mizhifei.dentist.systemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.systemadmin.dto.RateLimitConfigRequest;
import press.mizhifei.dentist.systemadmin.dto.RateLimitConfigResponse;
import press.mizhifei.dentist.systemadmin.model.RateLimitConfig;
import press.mizhifei.dentist.systemadmin.repository.RateLimitConfigRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced Rate Limit Configuration Service
 * 
 * Comprehensive service for managing rate limiting configurations
 * following SOLID principles and clean architecture patterns
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RateLimitConfigService {

    private final RateLimitConfigRepository rateLimitConfigRepository;
    private final ConfigurationRefreshService configurationRefreshService;

    /**
     * Get all active rate limit configurations
     */
    @Cacheable(value = "rateLimitConfigs", key = "'all'")
    public List<RateLimitConfigResponse> getAllConfigurations() {
        log.debug("Retrieving all active rate limit configurations");
        
        return rateLimitConfigRepository.findByActiveTrueOrderByPriorityDescCreatedAtAsc()
                .stream()
                .map(RateLimitConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get configuration by ID
     */
    @Cacheable(value = "rateLimitConfigs", key = "#id")
    public Optional<RateLimitConfigResponse> getConfigurationById(Long id) {
        log.debug("Retrieving rate limit configuration with ID: {}", id);
        
        return rateLimitConfigRepository.findById(id)
                .map(RateLimitConfigResponse::fromEntity);
    }

    /**
     * Get configuration by name
     */
    @Cacheable(value = "rateLimitConfigs", key = "'name:' + #configName")
    public Optional<RateLimitConfigResponse> getConfigurationByName(String configName) {
        log.debug("Retrieving rate limit configuration with name: {}", configName);
        
        return rateLimitConfigRepository.findByConfigName(configName)
                .map(RateLimitConfigResponse::fromEntity);
    }

    /**
     * Get configurations by service name
     */
    @Cacheable(value = "rateLimitConfigs", key = "'service:' + #serviceName")
    public List<RateLimitConfigResponse> getConfigurationsByService(String serviceName) {
        log.debug("Retrieving rate limit configurations for service: {}", serviceName);
        
        return rateLimitConfigRepository.findByServiceNameAndActiveTrueOrderByPriorityDescCreatedAtAsc(serviceName)
                .stream()
                .map(RateLimitConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get configurations by user role
     */
    @Cacheable(value = "rateLimitConfigs", key = "'role:' + #userRole")
    public List<RateLimitConfigResponse> getConfigurationsByRole(String userRole) {
        log.debug("Retrieving rate limit configurations for role: {}", userRole);
        
        return rateLimitConfigRepository.findByUserRoleAndActiveTrueOrderByPriorityDescCreatedAtAsc(userRole)
                .stream()
                .map(RateLimitConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get configurations by category
     */
    @Cacheable(value = "rateLimitConfigs", key = "'category:' + #category")
    public List<RateLimitConfigResponse> getConfigurationsByCategory(String category) {
        log.debug("Retrieving rate limit configurations for category: {}", category);
        
        return rateLimitConfigRepository.findByCategoryAndActiveTrueOrderByPriorityDescCreatedAtAsc(category)
                .stream()
                .map(RateLimitConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Find best matching configuration for a specific context
     */
    @Cacheable(value = "rateLimitConfigs", key = "'match:' + #serviceName + ':' + #userRole + ':' + #clinicId + ':' + #environment")
    public Optional<RateLimitConfigResponse> findBestMatchingConfiguration(
            String serviceName, String userRole, Long clinicId, String environment) {
        log.debug("Finding best matching configuration for service: {}, role: {}, clinic: {}, environment: {}", 
                serviceName, userRole, clinicId, environment);
        
        return rateLimitConfigRepository.findBestMatchingConfiguration(serviceName, userRole, clinicId, environment)
                .map(RateLimitConfigResponse::fromEntity);
    }

    /**
     * Create new rate limit configuration
     */
    @Transactional
    @CacheEvict(value = "rateLimitConfigs", allEntries = true)
    public RateLimitConfigResponse createConfiguration(RateLimitConfigRequest request, String createdBy) {
        log.info("Creating new rate limit configuration: {} by user: {}", request.getConfigName(), createdBy);
        
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rate limit configuration request");
        }
        
        // Check for duplicate name
        if (rateLimitConfigRepository.existsByConfigName(request.getConfigName())) {
            throw new IllegalArgumentException("Configuration with name '" + request.getConfigName() + "' already exists");
        }
        
        // Check for potential conflicts
        List<RateLimitConfig> conflicts = rateLimitConfigRepository.findPotentialConflicts(
                request.getServiceName(), request.getEndpointPattern(), -1L);
        if (!conflicts.isEmpty()) {
            log.warn("Potential conflicts detected for new configuration: {}", request.getConfigName());
        }
        
        // Create entity
        RateLimitConfig entity = request.toEntity();
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(createdBy);
        
        // Save entity
        RateLimitConfig saved = rateLimitConfigRepository.save(entity);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("Rate limit configuration created: " + saved.getConfigName());
        
        log.info("Successfully created rate limit configuration: {} with ID: {}", saved.getConfigName(), saved.getId());
        return RateLimitConfigResponse.fromEntity(saved);
    }

    /**
     * Update existing rate limit configuration
     */
    @Transactional
    @CacheEvict(value = "rateLimitConfigs", allEntries = true)
    public RateLimitConfigResponse updateConfiguration(Long id, RateLimitConfigRequest request, String updatedBy) {
        log.info("Updating rate limit configuration with ID: {} by user: {}", id, updatedBy);
        
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rate limit configuration request");
        }
        
        // Find existing configuration
        RateLimitConfig existing = rateLimitConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));
        
        // Check for duplicate name (excluding current)
        if (!existing.getConfigName().equals(request.getConfigName()) && 
            rateLimitConfigRepository.existsByConfigName(request.getConfigName())) {
            throw new IllegalArgumentException("Configuration with name '" + request.getConfigName() + "' already exists");
        }
        
        // Check for potential conflicts
        List<RateLimitConfig> conflicts = rateLimitConfigRepository.findPotentialConflicts(
                request.getServiceName(), request.getEndpointPattern(), id);
        if (!conflicts.isEmpty()) {
            log.warn("Potential conflicts detected for updated configuration: {}", request.getConfigName());
        }
        
        // Update entity
        updateEntityFromRequest(existing, request);
        existing.setUpdatedBy(updatedBy);
        
        // Save entity
        RateLimitConfig saved = rateLimitConfigRepository.save(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("Rate limit configuration updated: " + saved.getConfigName());
        
        log.info("Successfully updated rate limit configuration: {} with ID: {}", saved.getConfigName(), saved.getId());
        return RateLimitConfigResponse.fromEntity(saved);
    }

    /**
     * Delete rate limit configuration
     */
    @Transactional
    @CacheEvict(value = "rateLimitConfigs", allEntries = true)
    public void deleteConfiguration(Long id, String deletedBy) {
        log.info("Deleting rate limit configuration with ID: {} by user: {}", id, deletedBy);
        
        RateLimitConfig existing = rateLimitConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));
        
        String configName = existing.getConfigName();
        rateLimitConfigRepository.delete(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("Rate limit configuration deleted: " + configName);
        
        log.info("Successfully deleted rate limit configuration: {} with ID: {}", configName, id);
    }

    /**
     * Activate/deactivate configuration
     */
    @Transactional
    @CacheEvict(value = "rateLimitConfigs", allEntries = true)
    public RateLimitConfigResponse toggleConfiguration(Long id, boolean active, String updatedBy) {
        log.info("Toggling rate limit configuration with ID: {} to active: {} by user: {}", id, active, updatedBy);
        
        RateLimitConfig existing = rateLimitConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + id));
        
        existing.setActive(active);
        existing.setUpdatedBy(updatedBy);
        
        RateLimitConfig saved = rateLimitConfigRepository.save(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("Rate limit configuration " + (active ? "activated" : "deactivated") + ": " + saved.getConfigName());
        
        log.info("Successfully toggled rate limit configuration: {} to active: {}", saved.getConfigName(), active);
        return RateLimitConfigResponse.fromEntity(saved);
    }

    /**
     * Get configuration statistics
     */
    public ConfigurationStatistics getStatistics() {
        log.debug("Retrieving rate limit configuration statistics");
        
        List<Object[]> serviceStats = rateLimitConfigRepository.countConfigurationsByService();
        List<Object[]> categoryStats = rateLimitConfigRepository.countConfigurationsByCategory();
        
        return ConfigurationStatistics.builder()
                .totalConfigurations(rateLimitConfigRepository.count())
                .activeConfigurations(rateLimitConfigRepository.findByActiveTrueOrderByPriorityDescCreatedAtAsc().size())
                .serviceStats(serviceStats)
                .categoryStats(categoryStats)
                .build();
    }

    /**
     * Update entity from request
     */
    private void updateEntityFromRequest(RateLimitConfig entity, RateLimitConfigRequest request) {
        entity.setConfigName(request.getConfigName());
        entity.setServiceName(request.getServiceName());
        entity.setEndpointPattern(request.getEndpointPattern());
        entity.setUserRole(request.getUserRole());
        entity.setClinicId(request.getClinicId());
        entity.setMaxRequests(request.getMaxRequests());
        entity.setTimeWindowSeconds(request.getTimeWindowSeconds());
        entity.setLimitType(request.getLimitType());
        entity.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        entity.setActive(request.getActive() != null ? request.getActive() : true);
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory() != null ? request.getCategory() : "GENERAL");
        entity.setEnvironment(request.getEnvironment());
    }

    /**
     * Trigger configuration refresh
     */
    private void triggerConfigurationRefresh(String reason) {
        try {
            configurationRefreshService.refreshForRateLimitUpdate(reason);
        } catch (Exception e) {
            log.error("Failed to trigger configuration refresh: {}", e.getMessage(), e);
        }
    }

    /**
     * Configuration statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ConfigurationStatistics {
        private long totalConfigurations;
        private long activeConfigurations;
        private List<Object[]> serviceStats;
        private List<Object[]> categoryStats;
    }
}
