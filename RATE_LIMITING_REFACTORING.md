# Rate Limiting Refactoring - DentistDSS Microservices

## Overview

This document describes the comprehensive refactoring of rate limiting functionality in the DentistDSS microservices architecture, moving from service-level implementation to a centralized, configurable solution at the API Gateway level.

## Architectural Changes

### Before: Service-Level Rate Limiting
- **Location**: `genai-service/src/main/java/press/mizhifei/dentist/genai/service/TokenRateLimiter.java`
- **Scope**: Only GenAI service endpoints
- **Configuration**: Hard-coded (10,000 tokens per 3 minutes)
- **Limitations**: 
  - No centralized management
  - Service-specific implementation
  - No dynamic configuration
  - Limited to GenAI service only

### After: Centralized API Gateway Rate Limiting
- **Location**: `api-gateway/src/main/java/press/mizhifei/dentist/gateway/filter/RateLimitFilter.java`
- **Scope**: All microservices through API Gateway
- **Configuration**: Dynamic via `system-service`
- **Benefits**:
  - Centralized rate limiting for all services
  - Dynamic configuration management
  - Role-based and clinic-specific limits
  - Better separation of concerns
  - Improved scalability and maintainability

## Implementation Details

### 1. System Service - Configuration Management

#### New Components:
- **`RateLimitConfig`** - JPA entity for storing rate limit configurations
- **`RateLimitConfigService`** - Business logic for managing configurations
- **`RateLimitConfigController`** - REST API for configuration management
- **`RateLimitDataInitializer`** - Automatic setup of default configurations

#### Key Features:
- **Dynamic Configuration**: Update rate limits without service restarts
- **Role-Based Limits**: Different limits for different user roles (DENTIST, CLINIC_ADMIN, etc.)
- **Clinic-Specific Limits**: Per-clinic rate limiting capabilities
- **Priority-Based Matching**: Intelligent configuration resolution
- **Caching**: Caffeine-based caching for performance

#### Default Configurations:
```yaml
GenAI Service:
  - Default: 10,000 tokens per 3 minutes
  - Dentists: 20,000 tokens per 3 minutes  
  - Clinic Admins: 25,000 tokens per 3 minutes

Reporting Service:
  - Default: 100 requests per hour

Chat Log Service:
  - Default: 1,000 requests per hour

System Fallback:
  - Default: 500 requests per hour
```

### 2. API Gateway - Rate Limiting Implementation

#### New Components:
- **`RateLimitFilter`** - Global filter applying rate limits to all requests
- **`RateLimitService`** - Orchestrates rate limiting logic
- **`RateLimitConfigResolver`** - Resolves appropriate configurations
- **`BucketManager`** - Manages Bucket4j rate limiting buckets
- **`TokenEstimator`** - Estimates token consumption for requests
- **`RateLimitManagementController`** - Administrative endpoints

#### Architecture Principles Applied:
- **Single Responsibility Principle**: Each class has one clear responsibility
- **Open/Closed Principle**: Easy to extend with new rate limiting strategies
- **Dependency Inversion**: Depends on abstractions, not concrete implementations
- **Separation of Concerns**: Clear separation between configuration, bucket management, and token estimation

#### Key Features:
- **Distributed Rate Limiting**: Uses Bucket4j with Redis support
- **Intelligent Token Estimation**: Different strategies for different endpoint types
- **Graceful Degradation**: Allows requests on configuration errors
- **Comprehensive Logging**: Detailed logging for monitoring and debugging
- **Management Endpoints**: Administrative APIs for cache management and statistics

### 3. GenAI Service - Simplified Implementation

#### Removed Components:
- **`TokenRateLimiter.java`** - Completely removed
- **Rate limiting logic** - Removed from all controller endpoints
- **Bucket4j dependency** - Removed from build.gradle

#### Benefits:
- **Simplified codebase**: Focus on core AI functionality
- **Better performance**: No rate limiting overhead at service level
- **Improved maintainability**: Single responsibility for AI chat functionality

## Configuration Management

### API Endpoints

#### System Service (`/system/rate-limit`)
```bash
# Create or update configuration
POST /system/rate-limit
{
  "configName": "genai-premium-users",
  "serviceName": "genai-service",
  "endpointPattern": "/api/genai/",
  "userRole": "PREMIUM_USER",
  "maxRequests": 50000,
  "timeWindowSeconds": 180,
  "limitType": "TOKEN_COUNT",
  "priority": 25,
  "active": true,
  "description": "Higher limits for premium users"
}

# Get all configurations
GET /system/rate-limit

# Get active configurations
GET /system/rate-limit/active

# Find matching configuration
GET /system/rate-limit/match?endpoint=/api/genai/help&userRole=DENTIST&clinicId=123

# Toggle configuration status
PATCH /system/rate-limit/{configName}/toggle

# Delete configuration
DELETE /system/rate-limit/{configName}
```

#### API Gateway Management (`/management/rate-limit`)
```bash
# Clear rate limit cache
POST /management/rate-limit/cache/clear

# Get statistics
GET /management/rate-limit/stats

# Health check
GET /management/rate-limit/health
```

### Configuration Schema

```sql
CREATE TABLE rate_limit_configs (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(255) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    endpoint_pattern VARCHAR(255) NOT NULL,
    user_role VARCHAR(100),
    clinic_id BIGINT,
    max_requests BIGINT NOT NULL,
    time_window_seconds BIGINT NOT NULL,
    limit_type VARCHAR(50) NOT NULL,
    priority INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT true,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Performance Optimizations

### 1. Multi-Level Caching
- **L1 Cache**: Caffeine (in-memory, fast access)
- **L2 Cache**: Redis (distributed, larger capacity)
- **Configuration Caching**: 10-minute TTL for rate limit configurations
- **Bucket Caching**: Local bucket cache for performance

### 2. Efficient Bucket Management
- **Lazy Initialization**: Buckets created on-demand
- **Memory Management**: Automatic cleanup of unused buckets
- **Distributed Support**: Redis-based buckets for multi-instance deployments

### 3. Intelligent Token Estimation
- **Content-Based**: Estimates tokens based on request content
- **Endpoint-Specific**: Different estimation strategies per service
- **Fallback Values**: Safe defaults when estimation fails

## Security Considerations

### 1. Access Control
- **Role-Based Configuration**: Only admins can modify rate limit configurations
- **Audit Logging**: All configuration changes are logged
- **Secure Defaults**: Conservative default limits to prevent abuse

### 2. Error Handling
- **Graceful Degradation**: Allow requests when rate limiting fails
- **Circuit Breaker Pattern**: Prevent cascading failures
- **Comprehensive Monitoring**: Detailed metrics and alerting

## Monitoring and Observability

### 1. Metrics
- **Rate Limit Hits**: Number of requests blocked by rate limiting
- **Configuration Changes**: Tracking of configuration modifications
- **Bucket Statistics**: Active bucket count and memory usage
- **Performance Metrics**: Response times and error rates

### 2. Logging
- **Structured Logging**: JSON-formatted logs for easy parsing
- **Correlation IDs**: Request tracing across services
- **Debug Information**: Detailed logging for troubleshooting

### 3. Health Checks
- **Service Health**: Rate limiting service availability
- **Configuration Health**: Validation of rate limit configurations
- **Cache Health**: Cache hit rates and performance

## Migration Guide

### 1. Deployment Steps
1. **Deploy System Service**: With new rate limit configuration tables
2. **Deploy API Gateway**: With new rate limiting filter
3. **Deploy GenAI Service**: With rate limiting code removed
4. **Verify Configuration**: Ensure default configurations are loaded
5. **Test Rate Limiting**: Verify rate limits are working correctly

### 2. Rollback Plan
1. **Disable Rate Limit Filter**: Set filter order to lowest priority
2. **Revert GenAI Service**: Re-enable service-level rate limiting
3. **Monitor System**: Ensure no performance degradation
4. **Clean Up**: Remove new configurations if needed

### 3. Testing Strategy
- **Unit Tests**: Comprehensive test coverage for all components
- **Integration Tests**: End-to-end rate limiting verification
- **Load Tests**: Performance testing under high load
- **Chaos Engineering**: Failure scenario testing

## Benefits Achieved

### 1. Architectural Benefits
- **Better Separation of Concerns**: Rate limiting separated from business logic
- **Centralized Management**: Single point of configuration for all services
- **Improved Scalability**: Distributed rate limiting with Redis support
- **Enhanced Maintainability**: Cleaner, more focused service implementations

### 2. Operational Benefits
- **Dynamic Configuration**: No service restarts required for limit changes
- **Role-Based Limits**: Flexible limits based on user roles and clinics
- **Better Monitoring**: Comprehensive metrics and logging
- **Easier Troubleshooting**: Centralized rate limiting logic

### 3. Performance Benefits
- **Reduced Service Overhead**: No rate limiting logic in individual services
- **Efficient Caching**: Multi-level caching for optimal performance
- **Smart Token Estimation**: Accurate resource consumption estimation
- **Optimized Bucket Management**: Memory-efficient bucket lifecycle

## Future Enhancements

### 1. Advanced Features
- **Machine Learning**: AI-based rate limit optimization
- **Adaptive Limits**: Dynamic adjustment based on system load
- **Geographic Limits**: Location-based rate limiting
- **Time-Based Limits**: Different limits for different time periods

### 2. Integration Improvements
- **Prometheus Metrics**: Enhanced monitoring integration
- **Grafana Dashboards**: Visual monitoring and alerting
- **Kubernetes Integration**: Native K8s rate limiting support
- **Service Mesh**: Istio/Envoy integration for advanced traffic management

This refactoring represents a significant architectural improvement, providing a robust, scalable, and maintainable rate limiting solution that follows enterprise-grade best practices and SOLID principles.
