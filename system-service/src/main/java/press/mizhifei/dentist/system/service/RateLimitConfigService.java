package press.mizhifei.dentist.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.system.dto.RateLimitConfigRequest;
import press.mizhifei.dentist.system.dto.RateLimitConfigResponse;
import press.mizhifei.dentist.system.model.RateLimitConfig;
import press.mizhifei.dentist.system.repository.RateLimitConfigRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing Rate Limit Configurations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitConfigService {
    
    private final RateLimitConfigRepository repository;
    
    /**
     * Create or update rate limit configuration
     */
    @Transactional
    @CacheEvict(value = {"rateLimitConfigs", "rateLimitConfig"}, allEntries = true)
    public RateLimitConfigResponse createOrUpdate(RateLimitConfigRequest request) {
        log.info("Creating/updating rate limit config: {}", request.getConfigName());
        
        RateLimitConfig config = repository.findByConfigName(request.getConfigName())
                .map(existing -> updateExistingConfig(existing, request))
                .orElse(createNewConfig(request));
        
        RateLimitConfig saved = repository.save(config);
        log.info("Saved rate limit config with ID: {}", saved.getId());
        
        return toResponse(saved);
    }
    
    /**
     * Get all rate limit configurations
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rateLimitConfigs", key = "'all'")
    public List<RateLimitConfigResponse> getAllConfigurations() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active rate limit configurations
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rateLimitConfigs", key = "'active'")
    public List<RateLimitConfigResponse> getActiveConfigurations() {
        return repository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get configuration by name
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rateLimitConfig", key = "#configName")
    public Optional<RateLimitConfigResponse> getByConfigName(String configName) {
        return repository.findByConfigName(configName)
                .map(this::toResponse);
    }
    
    /**
     * Find best matching configuration for a request context
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rateLimitConfig", key = "#endpoint + '_' + #userRole + '_' + #clinicId")
    public Optional<RateLimitConfigResponse> findBestMatchingConfig(String endpoint, String userRole, Long clinicId) {
        List<RateLimitConfig> matches = repository.findBestMatchingConfiguration(endpoint, userRole, clinicId);
        
        if (!matches.isEmpty()) {
            RateLimitConfig bestMatch = matches.get(0); // Already ordered by priority and specificity
            log.debug("Found matching rate limit config: {} for endpoint: {}, role: {}, clinic: {}", 
                    bestMatch.getConfigName(), endpoint, userRole, clinicId);
            return Optional.of(toResponse(bestMatch));
        }
        
        log.debug("No matching rate limit config found for endpoint: {}, role: {}, clinic: {}", 
                endpoint, userRole, clinicId);
        return Optional.empty();
    }
    
    /**
     * Delete configuration
     */
    @Transactional
    @CacheEvict(value = {"rateLimitConfigs", "rateLimitConfig"}, allEntries = true)
    public void deleteConfiguration(String configName) {
        log.info("Deleting rate limit config: {}", configName);
        repository.findByConfigName(configName)
                .ifPresent(config -> {
                    repository.delete(config);
                    log.info("Deleted rate limit config: {}", configName);
                });
    }
    
    /**
     * Toggle configuration active status
     */
    @Transactional
    @CacheEvict(value = {"rateLimitConfigs", "rateLimitConfig"}, allEntries = true)
    public RateLimitConfigResponse toggleActive(String configName) {
        log.info("Toggling active status for rate limit config: {}", configName);
        
        RateLimitConfig config = repository.findByConfigName(configName)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configName));
        
        config.setActive(!config.getActive());
        RateLimitConfig saved = repository.save(config);
        
        log.info("Toggled rate limit config {} to active: {}", configName, saved.getActive());
        return toResponse(saved);
    }
    
    private RateLimitConfig createNewConfig(RateLimitConfigRequest request) {
        return RateLimitConfig.builder()
                .configName(request.getConfigName())
                .serviceName(request.getServiceName())
                .endpointPattern(request.getEndpointPattern())
                .userRole(request.getUserRole())
                .clinicId(request.getClinicId())
                .maxRequests(request.getMaxRequests())
                .timeWindowSeconds(request.getTimeWindowSeconds())
                .limitType(request.getLimitType())
                .priority(request.getPriority())
                .active(request.getActive())
                .description(request.getDescription())
                .build();
    }
    
    private RateLimitConfig updateExistingConfig(RateLimitConfig existing, RateLimitConfigRequest request) {
        existing.setServiceName(request.getServiceName());
        existing.setEndpointPattern(request.getEndpointPattern());
        existing.setUserRole(request.getUserRole());
        existing.setClinicId(request.getClinicId());
        existing.setMaxRequests(request.getMaxRequests());
        existing.setTimeWindowSeconds(request.getTimeWindowSeconds());
        existing.setLimitType(request.getLimitType());
        existing.setPriority(request.getPriority());
        existing.setActive(request.getActive());
        existing.setDescription(request.getDescription());
        return existing;
    }
    
    private RateLimitConfigResponse toResponse(RateLimitConfig config) {
        return RateLimitConfigResponse.builder()
                .id(config.getId())
                .configName(config.getConfigName())
                .serviceName(config.getServiceName())
                .endpointPattern(config.getEndpointPattern())
                .userRole(config.getUserRole())
                .clinicId(config.getClinicId())
                .maxRequests(config.getMaxRequests())
                .timeWindowSeconds(config.getTimeWindowSeconds())
                .limitType(config.getLimitType())
                .priority(config.getPriority())
                .active(config.getActive())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
