package press.mizhifei.dentist.systemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import press.mizhifei.dentist.systemadmin.dto.ConfigurationRefreshRequest;
import press.mizhifei.dentist.systemadmin.dto.ConfigurationRefreshResponse;
import press.mizhifei.dentist.systemadmin.model.ConfigurationRefreshLog;
import press.mizhifei.dentist.systemadmin.repository.ConfigurationRefreshLogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Configuration Refresh Service
 * 
 * Orchestrates configuration refresh operations across all microservices
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
public class ConfigurationRefreshService {

    private final ConfigurationRefreshLogRepository refreshLogRepository;
    private final RestTemplate restTemplate;
    
    // Service discovery endpoints for refresh
    private static final Map<String, String> SERVICE_REFRESH_ENDPOINTS = createServiceEndpointsMap();

    private static Map<String, String> createServiceEndpointsMap() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("api-gateway", "http://api-gateway:8080/actuator/refresh");
        endpoints.put("auth-service", "http://auth-service:8081/actuator/refresh");
        endpoints.put("clinic-admin-service", "http://clinic-admin-service:8083/actuator/refresh");
        endpoints.put("appointment-service", "http://appointment-service:8084/actuator/refresh");
        endpoints.put("clinical-records-service", "http://clinical-records-service:8085/actuator/refresh");
        endpoints.put("user-profile-service", "http://user-profile-service:8087/actuator/refresh");
        endpoints.put("genai-service", "http://genai-service:8088/actuator/refresh");
        endpoints.put("chat-log-service", "http://chat-log-service:8089/actuator/refresh");
        endpoints.put("reporting-service", "http://reporting-service:8090/actuator/refresh");
        endpoints.put("audit-service", "http://audit-service:8091/actuator/refresh");
        endpoints.put("notification-service", "http://notification-service:8092/actuator/refresh");
        return Collections.unmodifiableMap(endpoints);
    }

    /**
     * Refresh all services
     */
    @Async
    @Transactional
    public CompletableFuture<ConfigurationRefreshResponse> refreshAllServices(String initiatedBy, String reason) {
        log.info("Initiating refresh of all services by user: {}", initiatedBy);
        
        ConfigurationRefreshRequest request = ConfigurationRefreshRequest.refreshAll(reason);
        return executeRefreshOperation(request, initiatedBy);
    }

    /**
     * Refresh a single service
     */
    @Transactional
    public ConfigurationRefreshResponse refreshSingleService(String serviceName, String initiatedBy, String reason) {
        log.info("Initiating refresh of service: {} by user: {}", serviceName, initiatedBy);
        
        ConfigurationRefreshRequest request = ConfigurationRefreshRequest.refreshService(serviceName, reason);
        return executeRefreshOperationSync(request, initiatedBy);
    }

    /**
     * Refresh multiple services
     */
    @Async
    @Transactional
    public CompletableFuture<ConfigurationRefreshResponse> refreshBatchServices(
            List<String> serviceNames, String initiatedBy, String reason) {
        log.info("Initiating batch refresh of services: {} by user: {}", serviceNames, initiatedBy);
        
        ConfigurationRefreshRequest request = ConfigurationRefreshRequest.refreshBatch(serviceNames, reason);
        return executeRefreshOperation(request, initiatedBy);
    }

    /**
     * Refresh services affected by rate limit configuration changes
     */
    @Async
    @Transactional
    public CompletableFuture<ConfigurationRefreshResponse> refreshForRateLimitUpdate(String configChanges) {
        log.info("Initiating refresh for rate limit configuration update");
        
        ConfigurationRefreshRequest request = ConfigurationRefreshRequest.refreshForRateLimit(configChanges);
        return executeRefreshOperation(request, "SYSTEM");
    }

    /**
     * Refresh services affected by system parameter changes
     */
    @Async
    @Transactional
    public CompletableFuture<ConfigurationRefreshResponse> refreshForSystemParameterUpdate(String configChanges) {
        log.info("Initiating refresh for system parameter update");
        
        ConfigurationRefreshRequest request = ConfigurationRefreshRequest.refreshForSystemParam(configChanges);
        return executeRefreshOperation(request, "SYSTEM");
    }

    /**
     * Get refresh operation status
     */
    public Optional<ConfigurationRefreshResponse> getRefreshStatus(String refreshId) {
        log.debug("Retrieving refresh status for ID: {}", refreshId);
        
        return refreshLogRepository.findByRefreshId(refreshId)
                .map(ConfigurationRefreshResponse::fromEntity);
    }

    /**
     * Get recent refresh operations
     */
    public List<ConfigurationRefreshResponse> getRecentRefreshOperations(int hours) {
        log.debug("Retrieving refresh operations from last {} hours", hours);
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return refreshLogRepository.findRecentLogs(since)
                .stream()
                .map(ConfigurationRefreshResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get refresh operations by status
     */
    public List<ConfigurationRefreshResponse> getRefreshOperationsByStatus(
            ConfigurationRefreshLog.RefreshStatus status) {
        log.debug("Retrieving refresh operations with status: {}", status);
        
        return refreshLogRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(ConfigurationRefreshResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Execute refresh operation asynchronously
     */
    private CompletableFuture<ConfigurationRefreshResponse> executeRefreshOperation(
            ConfigurationRefreshRequest request, String initiatedBy) {
        
        return CompletableFuture.supplyAsync(() -> executeRefreshOperationSync(request, initiatedBy));
    }

    /**
     * Execute refresh operation synchronously
     */
    private ConfigurationRefreshResponse executeRefreshOperationSync(
            ConfigurationRefreshRequest request, String initiatedBy) {
        
        String refreshId = generateRefreshId();
        LocalDateTime startTime = LocalDateTime.now();
        
        // Create initial log entry
        ConfigurationRefreshLog refreshLog = ConfigurationRefreshLog.builder()
                .refreshId(refreshId)
                .refreshType(request.getRefreshType())
                .targetServices(request.getTargetServicesString())
                .status(ConfigurationRefreshLog.RefreshStatus.INITIATED)
                .initiatedBy(initiatedBy)
                .reason(request.getReason())
                .configurationChanges(request.getConfigurationChanges())
                .totalServices(request.getTotalServices())
                .build();
        
        refreshLog = refreshLogRepository.save(refreshLog);
        
        try {
            // Update status to in progress
            refreshLog.setStatus(ConfigurationRefreshLog.RefreshStatus.IN_PROGRESS);
            refreshLogRepository.save(refreshLog);
            
            // Execute refresh operations
            Map<String, ServiceRefreshResult> results = performRefreshOperations(request);
            
            // Calculate results
            int successful = (int) results.values().stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            int failed = results.size() - successful;
            
            // Determine final status
            ConfigurationRefreshLog.RefreshStatus finalStatus;
            if (failed == 0) {
                finalStatus = ConfigurationRefreshLog.RefreshStatus.COMPLETED;
            } else if (successful > 0) {
                finalStatus = ConfigurationRefreshLog.RefreshStatus.PARTIAL_SUCCESS;
            } else {
                finalStatus = ConfigurationRefreshLog.RefreshStatus.FAILED;
            }
            
            // Update log with results
            refreshLog.updateServiceCounts(successful, failed);
            refreshLog.markCompleted(finalStatus, formatResults(results));
            
            refreshLogRepository.save(refreshLog);
            
            log.info("Refresh operation {} completed with status: {} ({}/{} successful)", 
                    refreshId, finalStatus, successful, results.size());
            
        } catch (Exception e) {
            log.error("Refresh operation {} failed with error: {}", refreshId, e.getMessage(), e);
            
            refreshLog.setStatus(ConfigurationRefreshLog.RefreshStatus.FAILED);
            refreshLog.setErrorMessage(e.getMessage());
            refreshLog.setCompletedAt(LocalDateTime.now());
            
            refreshLogRepository.save(refreshLog);
        }
        
        return ConfigurationRefreshResponse.fromEntity(refreshLog);
    }

    /**
     * Perform actual refresh operations on services
     */
    private Map<String, ServiceRefreshResult> performRefreshOperations(ConfigurationRefreshRequest request) {
        Map<String, ServiceRefreshResult> results = new ConcurrentHashMap<>();
        
        List<String> targetServices = getTargetServicesList(request);
        
        // Execute refresh operations in parallel
        targetServices.parallelStream().forEach(serviceName -> {
            ServiceRefreshResult result = refreshSingleServiceInternal(serviceName);
            results.put(serviceName, result);
        });
        
        return results;
    }

    /**
     * Get list of target services based on request type
     */
    private List<String> getTargetServicesList(ConfigurationRefreshRequest request) {
        switch (request.getRefreshType()) {
            case SINGLE_SERVICE:
                return List.of(request.getServiceName());
            case BATCH_SERVICES:
                return request.getServiceNames();
            case ALL_SERVICES:
                return new ArrayList<>(SERVICE_REFRESH_ENDPOINTS.keySet());
            case RATE_LIMIT_UPDATE:
            case SYSTEM_PARAM_UPDATE:
                // For configuration updates, refresh most application services
                return List.of(
                    "api-gateway", "auth-service", "clinic-admin-service",
                    "appointment-service", "clinical-records-service", "user-profile-service",
                    "genai-service", "chat-log-service", "reporting-service",
                    "audit-service", "notification-service"
                );
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Refresh a single service internally
     */
    private ServiceRefreshResult refreshSingleServiceInternal(String serviceName) {
        long startTime = System.currentTimeMillis();
        
        try {
            String endpoint = SERVICE_REFRESH_ENDPOINTS.get(serviceName);
            if (endpoint == null) {
                return ServiceRefreshResult.builder()
                        .serviceName(serviceName)
                        .success(false)
                        .message("Service endpoint not configured")
                        .durationMs(System.currentTimeMillis() - startTime)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            // Call the refresh endpoint
            restTemplate.postForObject(endpoint, null, String.class);
            
            return ServiceRefreshResult.builder()
                    .serviceName(serviceName)
                    .success(true)
                    .message("Configuration refreshed successfully")
                    .durationMs(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to refresh service {}: {}", serviceName, e.getMessage());
            
            return ServiceRefreshResult.builder()
                    .serviceName(serviceName)
                    .success(false)
                    .message("Refresh failed: " + e.getMessage())
                    .durationMs(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Format refresh results for storage
     */
    private String formatResults(Map<String, ServiceRefreshResult> results) {
        StringBuilder sb = new StringBuilder();
        
        results.forEach((serviceName, result) -> {
            sb.append(serviceName).append(": ");
            sb.append(result.isSuccess() ? "SUCCESS" : "FAILED");
            if (!result.isSuccess()) {
                sb.append(" (").append(result.getMessage()).append(")");
            }
            sb.append("\n");
        });
        
        return sb.toString();
    }

    /**
     * Generate unique refresh ID
     */
    private String generateRefreshId() {
        return "REFRESH_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Service refresh result
     */
    @lombok.Data
    @lombok.Builder
    public static class ServiceRefreshResult {
        private String serviceName;
        private boolean success;
        private String message;
        private Long durationMs;
        private LocalDateTime timestamp;
    }
}
