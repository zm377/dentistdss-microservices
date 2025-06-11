package com.dentistdss.systemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.systemadmin.dto.SystemParameterRequest;
import com.dentistdss.systemadmin.dto.SystemParameterResponse;
import com.dentistdss.systemadmin.model.SystemParameter;
import com.dentistdss.systemadmin.repository.SystemParameterRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced System Parameter Service
 * 
 * Comprehensive service for managing system parameters
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
public class SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;
    private final ConfigurationRefreshService configurationRefreshService;

    /**
     * Get all active system parameters
     */
    @Cacheable(value = "systemParameters", key = "'all'")
    public List<SystemParameterResponse> getAllParameters() {
        log.debug("Retrieving all active system parameters");
        
        return systemParameterRepository.findByActiveTrueOrderByCategoryAscParameterKeyAsc()
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get parameter by ID
     */
    @Cacheable(value = "systemParameters", key = "#id")
    public Optional<SystemParameterResponse> getParameterById(Long id) {
        log.debug("Retrieving system parameter with ID: {}", id);
        
        return systemParameterRepository.findById(id)
                .map(SystemParameterResponse::fromEntity);
    }

    /**
     * Get parameter by key
     */
    @Cacheable(value = "systemParameters", key = "'key:' + #parameterKey")
    public Optional<SystemParameterResponse> getParameterByKey(String parameterKey) {
        log.debug("Retrieving system parameter with key: {}", parameterKey);
        
        return systemParameterRepository.findByParameterKey(parameterKey)
                .map(SystemParameterResponse::fromEntity);
    }

    /**
     * Get parameters by category
     */
    @Cacheable(value = "systemParameters", key = "'category:' + #category")
    public List<SystemParameterResponse> getParametersByCategory(SystemParameter.ParameterCategory category) {
        log.debug("Retrieving system parameters for category: {}", category);
        
        return systemParameterRepository.findByCategoryAndActiveTrueOrderByParameterKeyAsc(category)
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get parameters by service name
     */
    @Cacheable(value = "systemParameters", key = "'service:' + #serviceName")
    public List<SystemParameterResponse> getParametersByService(String serviceName) {
        log.debug("Retrieving system parameters for service: {}", serviceName);
        
        return systemParameterRepository.findByServiceNameAndActiveTrueOrderByParameterKeyAsc(serviceName)
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get global parameters (no specific service)
     */
    @Cacheable(value = "systemParameters", key = "'global'")
    public List<SystemParameterResponse> getGlobalParameters() {
        log.debug("Retrieving global system parameters");
        
        return systemParameterRepository.findByServiceNameIsNullAndActiveTrueOrderByParameterKeyAsc()
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get parameters for specific service and environment
     */
    @Cacheable(value = "systemParameters", key = "'serviceEnv:' + #serviceName + ':' + #environment")
    public List<SystemParameterResponse> getParametersForServiceAndEnvironment(String serviceName, String environment) {
        log.debug("Retrieving system parameters for service: {} and environment: {}", serviceName, environment);
        
        return systemParameterRepository.findParametersForServiceAndEnvironment(serviceName, environment)
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search parameters by key or name
     */
    public List<SystemParameterResponse> searchParameters(String searchTerm) {
        log.debug("Searching system parameters with term: {}", searchTerm);
        
        return systemParameterRepository.searchParameters(searchTerm)
                .stream()
                .map(SystemParameterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create new system parameter
     */
    @Transactional
    @CacheEvict(value = "systemParameters", allEntries = true)
    public SystemParameterResponse createParameter(SystemParameterRequest request, String createdBy) {
        log.info("Creating new system parameter: {} by user: {}", request.getParameterKey(), createdBy);
        
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid system parameter request");
        }
        
        // Check for duplicate key
        if (systemParameterRepository.existsByParameterKey(request.getParameterKey())) {
            throw new IllegalArgumentException("Parameter with key '" + request.getParameterKey() + "' already exists");
        }
        
        // Create entity
        SystemParameter entity = request.toEntity();
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(createdBy);
        
        // Save entity
        SystemParameter saved = systemParameterRepository.save(entity);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("System parameter created: " + saved.getParameterKey());
        
        log.info("Successfully created system parameter: {} with ID: {}", saved.getParameterKey(), saved.getId());
        return SystemParameterResponse.fromEntity(saved);
    }

    /**
     * Update existing system parameter
     */
    @Transactional
    @CacheEvict(value = "systemParameters", allEntries = true)
    public SystemParameterResponse updateParameter(Long id, SystemParameterRequest request, String updatedBy) {
        log.info("Updating system parameter with ID: {} by user: {}", id, updatedBy);
        
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid system parameter request");
        }
        
        // Find existing parameter
        SystemParameter existing = systemParameterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found with ID: " + id));
        
        // Check for duplicate key (excluding current)
        if (!existing.getParameterKey().equals(request.getParameterKey()) && 
            systemParameterRepository.existsByParameterKey(request.getParameterKey())) {
            throw new IllegalArgumentException("Parameter with key '" + request.getParameterKey() + "' already exists");
        }
        
        // Validate parameter value
        if (request.getParameterValue() != null && !existing.isValidValue(request.getParameterValue())) {
            throw new IllegalArgumentException("Invalid parameter value for key: " + request.getParameterKey());
        }
        
        // Update entity
        updateEntityFromRequest(existing, request);
        existing.setUpdatedBy(updatedBy);
        
        // Save entity
        SystemParameter saved = systemParameterRepository.save(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("System parameter updated: " + saved.getParameterKey());
        
        log.info("Successfully updated system parameter: {} with ID: {}", saved.getParameterKey(), saved.getId());
        return SystemParameterResponse.fromEntity(saved);
    }

    /**
     * Update parameter value only
     */
    @Transactional
    @CacheEvict(value = "systemParameters", allEntries = true)
    public SystemParameterResponse updateParameterValue(String parameterKey, String value, String updatedBy) {
        log.info("Updating system parameter value for key: {} by user: {}", parameterKey, updatedBy);
        
        SystemParameter existing = systemParameterRepository.findByParameterKey(parameterKey)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found with key: " + parameterKey));
        
        // Validate value
        if (value != null && !existing.isValidValue(value)) {
            throw new IllegalArgumentException("Invalid parameter value for key: " + parameterKey);
        }
        
        existing.setParameterValue(value);
        existing.setUpdatedBy(updatedBy);
        
        SystemParameter saved = systemParameterRepository.save(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("System parameter value updated: " + saved.getParameterKey());
        
        log.info("Successfully updated parameter value for key: {}", parameterKey);
        return SystemParameterResponse.fromEntity(saved);
    }

    /**
     * Delete system parameter
     */
    @Transactional
    @CacheEvict(value = "systemParameters", allEntries = true)
    public void deleteParameter(Long id, String deletedBy) {
        log.info("Deleting system parameter with ID: {} by user: {}", id, deletedBy);
        
        SystemParameter existing = systemParameterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found with ID: " + id));
        
        String parameterKey = existing.getParameterKey();
        systemParameterRepository.delete(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("System parameter deleted: " + parameterKey);
        
        log.info("Successfully deleted system parameter: {} with ID: {}", parameterKey, id);
    }

    /**
     * Activate/deactivate parameter
     */
    @Transactional
    @CacheEvict(value = "systemParameters", allEntries = true)
    public SystemParameterResponse toggleParameter(Long id, boolean active, String updatedBy) {
        log.info("Toggling system parameter with ID: {} to active: {} by user: {}", id, active, updatedBy);
        
        SystemParameter existing = systemParameterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found with ID: " + id));
        
        existing.setActive(active);
        existing.setUpdatedBy(updatedBy);
        
        SystemParameter saved = systemParameterRepository.save(existing);
        
        // Trigger configuration refresh
        triggerConfigurationRefresh("System parameter " + (active ? "activated" : "deactivated") + ": " + saved.getParameterKey());
        
        log.info("Successfully toggled system parameter: {} to active: {}", saved.getParameterKey(), active);
        return SystemParameterResponse.fromEntity(saved);
    }

    /**
     * Get parameter statistics
     */
    public ParameterStatistics getStatistics() {
        log.debug("Retrieving system parameter statistics");
        
        List<Object[]> categoryStats = systemParameterRepository.countParametersByCategory();
        List<Object[]> serviceStats = systemParameterRepository.countParametersByService();
        List<Object[]> dataTypeStats = systemParameterRepository.countParametersByDataType();
        
        return ParameterStatistics.builder()
                .totalParameters(systemParameterRepository.count())
                .activeParameters(systemParameterRepository.findByActiveTrueOrderByCategoryAscParameterKeyAsc().size())
                .sensitiveParameters(systemParameterRepository.findBySensitiveTrueAndActiveTrueOrderByParameterKeyAsc().size())
                .requiredParameters(systemParameterRepository.findByRequiredTrueAndActiveTrueOrderByParameterKeyAsc().size())
                .categoryStats(categoryStats)
                .serviceStats(serviceStats)
                .dataTypeStats(dataTypeStats)
                .build();
    }

    /**
     * Get parameters as key-value map for a service
     */
    @Cacheable(value = "systemParameters", key = "'map:' + #serviceName + ':' + #environment")
    public Map<String, Object> getParametersAsMap(String serviceName, String environment) {
        log.debug("Retrieving system parameters as map for service: {} and environment: {}", serviceName, environment);
        
        return systemParameterRepository.findParametersForServiceAndEnvironment(serviceName, environment)
                .stream()
                .collect(Collectors.toMap(
                    SystemParameter::getParameterKey,
                    SystemParameter::getTypedValue,
                    (existing, replacement) -> existing
                ));
    }

    /**
     * Update entity from request
     */
    private void updateEntityFromRequest(SystemParameter entity, SystemParameterRequest request) {
        entity.setParameterKey(request.getParameterKey());
        entity.setParameterName(request.getParameterName());
        entity.setParameterValue(request.getParameterValue());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setDataType(request.getDataType());
        entity.setCategory(request.getCategory());
        entity.setServiceName(request.getServiceName());
        entity.setEnvironment(request.getEnvironment());
        entity.setDescription(request.getDescription());
        entity.setSensitive(request.getSensitive() != null ? request.getSensitive() : false);
        entity.setRequired(request.getRequired() != null ? request.getRequired() : false);
        entity.setActive(request.getActive() != null ? request.getActive() : true);
        entity.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : false);
        entity.setValidationPattern(request.getValidationPattern());
        entity.setMinValue(request.getMinValue());
        entity.setMaxValue(request.getMaxValue());
        entity.setAllowedValues(request.getAllowedValues());
    }

    /**
     * Trigger configuration refresh
     */
    private void triggerConfigurationRefresh(String reason) {
        try {
            configurationRefreshService.refreshForSystemParameterUpdate(reason);
        } catch (Exception e) {
            log.error("Failed to trigger configuration refresh: {}", e.getMessage(), e);
        }
    }

    /**
     * Parameter statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ParameterStatistics {
        private long totalParameters;
        private long activeParameters;
        private long sensitiveParameters;
        private long requiredParameters;
        private List<Object[]> categoryStats;
        private List<Object[]> serviceStats;
        private List<Object[]> dataTypeStats;
    }
}
