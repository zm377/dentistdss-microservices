# DentistDSS Microservices

A microservices-based Dentist Decision Support System with centralized JWT authentication.

**Live Demo**: [https://dentistdss.com/](https://dentistdss.com/)
**Repository**: [https://github.com/zm377/dentistdss-microservices](https://github.com/zm377/dentistdss-microservices)

## Architecture Overview

Centralized security architecture where API Gateway handles JWT validation while microservices focus on business logic.

### Package Structure

The DentistDSS microservices follow enterprise Java naming conventions:

- **Group ID**: `com.dentistdss`
- **Base Package**: `com.dentistdss.*`
- **Service Packages**:
  - `com.dentistdss.gateway` - API Gateway
  - `com.dentistdss.auth` - Authentication Service
  - `com.dentistdss.clinicadmin` - Clinic Administration Service
  - `com.dentistdss.systemadmin` - System Administration Service
  - `com.dentistdss.genai` - GenAI Service
  - `com.dentistdss.chatlog` - Chat Log Service
  - `com.dentistdss.reporting` - Reporting Service
  - `com.dentistdss.userprofile` - User Profile Service
  - `com.dentistdss.appointment` - Appointment Service
  - `com.dentistdss.clinicalrecords` - Clinical Records Service
  - `com.dentistdss.audit` - Audit Service
  - `com.dentistdss.notification` - Notification Service
  - `com.dentistdss.discovery` - Discovery Server
  - `com.dentistdss.config` - Config Server
  - `com.dentistdss.admin` - Admin Server

## Services (11 microservices + 2 databases)

### Databases
- **PostgreSQL (v17)**: Port 5432 - User accounts, clinic data, patient records
- **MongoDB (v7.0)**: Port 27017 - AI conversations, audit logs

### Infrastructure Services
- **Config Server**: Port 8888 - Centralized configuration management
- **Discovery Server (Eureka)**: Port 8761 - Service discovery and registration
- **API Gateway**: Port 443/8080 - JWT validation, routing, HTTPS termination

### Core Business Services
- **Auth Service**: Port 8081 - JWT tokens, OAuth2, user management
- **Clinic Service**: Port 8083 - Clinic operations, staff, appointments
- **Patient Service**: Port 8085 - Patient profiles, medical history
- **System Service**: Port 8086 - System configuration, user roles

### Other Services
- **Notification Service**: Port 8088 - Email, SMS, push notifications
- **GenAI Service**: Port 8084 - AI chatbots, OpenAI integration
- **Audit Service**: Port 8087 - Activity logging, compliance
- **Admin Server**: Port 9090 - Service monitoring, health checks

## Quick Start

### Prerequisites
- Docker and Docker Compose
- JDK 21 (for local development)
- Gradle (wrapper included)

### Local Development
```bash
# Start all services locally
./start-local.sh

# Stop all services
./stop-local.sh

# Start only databases
docker-compose up -d postgres mongo
```

### Docker Deployment
```bash
# Start all services in Docker
./start-docker.sh

# Stop services (preserves databases)
./stop-docker.sh

# Check service status
./service-status.sh

# Stop everything including databases
docker-compose down -v
```

## Available Scripts

| Script | Purpose |
|--------|---------|
| `start-local.sh` | Start all services locally |
| `stop-local.sh` | Stop local services |
| `start-docker.sh` | Start services in Docker |
| `stop-docker.sh` | Stop Docker services |
| `service-status.sh` | Check service status |
| `test-gateway-security.sh` | Test API security |
| `build-and-push.sh [version]` | Build and push images |
| `deploy.sh [version]` | Deploy to production |

## Security Testing

```bash
# Test API Gateway security
./test-gateway-security.sh

# Manual tests
curl http://localhost:8080/api/clinic-admin/clinics  # Public endpoint
curl http://localhost:8080/api/clinic/1/patients  # Should return 401
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/clinic/1/patients
```

## Service Endpoints

| Service | Port | URL |
|---------|------|-----|
| API Gateway | 443/8080 | https://api.dentistdss.com |
| Config Server | 8888 | http://localhost:8888 |
| Eureka | 8761 | http://localhost:8761 |
| Auth Service | 8081 | http://localhost:8081 |
| Clinic Service | 8083 | http://localhost:8083 |
| GenAI Service | 8084 | http://localhost:8084 |
| Patient Service | 8085 | http://localhost:8085 |
| System Service | 8086 | http://localhost:8086 |
| Audit Service | 8087 | http://localhost:8087 |
| Notification Service | 8088 | http://localhost:8088 |
| Admin Server | 9090 | http://localhost:9090 |
| PostgreSQL | 5432 | localhost:5432 |
| MongoDB | 27017 | localhost:27017 |

## Security Architecture

### Centralized JWT Authentication
API Gateway handles all JWT validation and authorization. Business services receive user context via HTTP headers.

**Flow**: Client → API Gateway → JWT Validation → Headers → Business Service

### Roles
| Role | Access | Filtering |
|------|--------|-----------|
| SYSTEM_ADMIN | All endpoints | None |
| CLINIC_ADMIN | Clinic-specific | Own clinic |
| RECEPTIONIST | Clinic-specific | Own clinic |
| DENTIST | Clinical data | Clinic-based |
| PATIENT | Own records | Own data |

### Public Endpoints (No Auth Required)
- Auth: `/api/auth/login`, `/api/auth/signup`, `/oauth2/*`
- Clinic: `/api/clinic-admin/clinics`
- AI Chatbots: `/api/genai/chatbot/*`
- Health: `/actuator/health`

### User Context Headers
```http
X-User-ID: 12345
X-User-Email: user@example.com
X-User-Roles: CLINIC_ADMIN,DENTIST
X-Clinic-ID: 67890
```

## Development

### Adding Protected Endpoints
1. Update `JwtAuthenticationFilter.java` in API Gateway
2. Use `UserContextUtil` in service controllers:

```java
@GetMapping("/my-endpoint")
public ResponseEntity<?> myEndpoint(HttpServletRequest request) {
    String userId = UserContextUtil.getUserId(request);
    List<String> roles = UserContextUtil.getUserRoles(request);
    // Business logic
}
```

### Running Individual Services
```bash
# Local development
cd <service-name>
../gradlew bootRun

# Docker development
docker-compose up --build -d <service-name>
```

### Environment Variables
```bash
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
OPENAI_API_KEY=your_openai_api_key
```

## Production Deployment

```bash
# Build and push images
./build-and-push.sh [version]

# Deploy to production
./deploy.sh [version]
```

Images published to Docker Hub: `zm377/dentistdss-microservices:<service>-<tag>`

## Recent Updates

### OAuth Service Consolidation
- Merged OAuth-Service into Auth-Service (reduced from 2 to 1 authentication service)
- Improved performance with direct method calls instead of HTTP requests
- Simplified deployment and maintenance

### Architecture Benefits
- **Centralized Security**: All JWT validation in API Gateway
- **Clean Separation**: Business services focus on domain logic
- **Better Performance**: Reduced JWT parsing overhead
- **Easy Maintenance**: Security updates only in API Gateway

## Enhanced Features

### 1. Anonymous User Tracking
- Secure session management across microservices
- Identity stitching when users authenticate
- Unified session identifier for anonymous and authenticated users

### 2. Prompt Orchestration
- Dynamic prompt templates with MongoDB storage
- Role-based and clinic-specific customization
- Template variable substitution

### 3. AI Provider Switching
- Multi-provider support (OpenAI, Google Vertex AI)
- Automatic failover and load balancing
- Configuration-driven provider management

## Configuration Management

### Spring Cloud Config Server

The system uses Spring Cloud Config Server for centralized configuration management following 12-factor app principles.

#### Configuration Sources
- **External Git Repository**: Primary configuration source (recommended for production)
- **Local Classpath**: Fallback configuration source
- **Environment Variables**: Override sensitive data and environment-specific settings

#### Configuration Hierarchy (highest to lowest priority)
1. Environment variables
2. `{service}-{profile}.yml` from Git repository
3. `{service}.yml` from Git repository
4. `application-{profile}.yml` from Git repository
5. `application.yml` from Git repository
6. Local classpath configurations

#### Setup External Configuration Repository

1. **Create Git Repository**: Create a repository (e.g., `dentistdss-config`) with the following structure:
```
dentistdss-config/
├── application.yml                    # Global defaults
├── application-dev.yml               # Development environment
├── application-docker.yml            # Docker environment
├── application-prod.yml              # Production environment
├── api-gateway/
│   ├── api-gateway.yml               # Service-specific config
│   ├── api-gateway-dev.yml
│   ├── api-gateway-docker.yml
│   └── api-gateway-prod.yml
├── genai-service/
│   ├── genai-service.yml
│   └── genai-service-{profile}.yml
└── [other-services]/
    ├── {service}.yml
    └── {service}-{profile}.yml
```

2. **Configure Config Server**: Set environment variables:
```bash
CONFIG_GIT_URI=https://github.com/your-org/dentistdss-config
CONFIG_GIT_USERNAME=your_username  # For private repos
CONFIG_GIT_PASSWORD=your_token      # For private repos
```

3. **Hot Reload Configuration**: Use refresh endpoints to update configuration without restart:
```bash
# Refresh specific service
curl -X POST http://localhost:8080/actuator/refresh

# Refresh all services (via API Gateway)
curl -X POST http://localhost:8080/management/refresh-all
```

### Environment Variables

#### Core Configuration
```bash
# Config Server
CONFIG_GIT_URI=https://github.com/your-org/dentistdss-config
CONFIG_GIT_USERNAME=your_username
CONFIG_GIT_PASSWORD=your_token
SPRING_CONFIG_USER=configuser
SPRING_CONFIG_PASS=configpass

# Service Discovery
EUREKA_URI=http://localhost:8761/eureka

# Database Configuration
POSTGRES_PASSWORD=your_postgres_password
POSTGRES_READONLY_PASSWORD=your_readonly_password
MONGO_INITDB_ROOT_PASSWORD=your_mongo_password
REDIS_PASSWORD=your_redis_password

# Security
JWT_SECRET=your_jwt_secret_key
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

#### AI Provider Configuration
```bash
# OpenAI (Required)
OPENAI_API_KEY=your_openai_api_key

# Google Vertex AI (Optional)
VERTEX_AI_ENABLED=false
VERTEX_AI_PROJECT_ID=your_gcp_project_id
VERTEX_AI_LOCATION=us-central1
GEMINI_MODEL=gemini-2.5-pro-preview-05-06

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

#### Environment-Specific Variables

**Development:**
```bash
SPRING_PROFILES_ACTIVE=dev
EUREKA_URI=http://localhost:8761/eureka
FRONTEND_URL=http://localhost:3000
```

**Docker:**
```bash
SPRING_PROFILES_ACTIVE=docker
EUREKA_URI=http://discovery-server:8761/eureka
POSTGRES_HOST=postgres
MONGO_HOST=mongo
REDIS_HOST=redis
```

**Production:**
```bash
SPRING_PROFILES_ACTIVE=prod
EUREKA_URI=https://discovery.yourdomain.com/eureka
FRONTEND_URL=https://yourdomain.com
HOSTNAME=${HOSTNAME}
```

## Configuration Refresh and Hot Reload

### Automatic Configuration Refresh

The system supports hot configuration reload without service restarts:

#### Manual Refresh
```bash
# Refresh specific service configuration
curl -X POST http://localhost:8080/actuator/refresh

# Refresh API Gateway configuration
curl -X POST http://localhost:8080/actuator/refresh

# Check current configuration
curl http://localhost:8080/actuator/configprops
curl http://localhost:8080/actuator/env
```

#### Webhook-Based Refresh (Production)
Configure Git repository webhooks to automatically refresh configuration:

1. **GitHub Webhook**: Configure webhook URL: `https://your-domain.com/monitor`
2. **GitLab Webhook**: Configure webhook URL: `https://your-domain.com/monitor`
3. **Automatic Refresh**: Services automatically refresh when configuration changes

#### Configuration Validation
```bash
# Verify config server is accessible
curl http://localhost:8888/actuator/health

# Test configuration retrieval
curl http://localhost:8888/api-gateway/dev
curl http://localhost:8888/genai-service/docker

# Check service configuration
curl http://localhost:8080/actuator/configprops | jq '.configurationProperties'
```

## Testing

```bash
# Run all tests
./gradlew test

# Test specific services
./gradlew :api-gateway:test :genai-service:test :auth-service:test

# Test configuration refresh
./test-config-refresh.sh

# Test API endpoints
curl -X POST http://localhost:8080/api/genai/chatbot/help \
  -H "Content-Type: application/json" \
  -d '"What are your clinic hours?"'

# Test rate limiting
curl -X POST http://localhost:8080/api/genai/help \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token" \
  -d '"Test rate limiting"'
```

## Advanced Features

### Rate Limiting
- **Centralized Rate Limiting**: Implemented at API Gateway level
- **Dynamic Configuration**: Rate limits configurable via system-service
- **Role-Based Limits**: Different limits for different user roles
- **Token-Based Limiting**: Intelligent token consumption estimation for AI services

### Reporting and Analytics
- **Advanced Analytics Engine**: Complex SQL queries with read replica optimization
- **Multi-Format Reports**: PDF, Excel, CSV with professional formatting
- **Automated Scheduling**: Configurable report schedules with email delivery
- **Performance Optimization**: Async processing, caching, and materialized views

### Chat Logging and Audit
- **Comprehensive Chat Logging**: All AI interactions logged with PHI redaction
- **Token Usage Tracking**: Detailed token consumption and cost analysis
- **Audit Trail**: Complete audit trail for compliance and monitoring
- **Performance Metrics**: Response times, success rates, and usage patterns

## Troubleshooting

### Common Issues

#### Config Server Connection Issues
```bash
# Check config server health
curl http://localhost:8888/actuator/health

# Verify Git repository access
curl http://localhost:8888/actuator/env | grep git

# Check service registration
curl http://localhost:8761/eureka/apps
```

#### Configuration Not Refreshing
```bash
# Verify refresh endpoint is enabled
curl http://localhost:8080/actuator/refresh

# Check configuration properties
curl http://localhost:8080/actuator/configprops

# Force refresh all services
curl -X POST http://localhost:8080/management/refresh-all
```

#### Rate Limiting Issues
```bash
# Check rate limit configuration
curl http://localhost:8086/system/rate-limit/active

# Clear rate limit cache
curl -X POST http://localhost:8080/management/rate-limit/cache/clear

# Check rate limit statistics
curl http://localhost:8080/management/rate-limit/stats
```

## Documentation

For detailed information:
- **[ARCHITECTURE_GUIDE.md](ARCHITECTURE_GUIDE.md)** - Comprehensive architecture guide
- **[OAUTH_CONSOLIDATION_SUMMARY.md](OAUTH_CONSOLIDATION_SUMMARY.md)** - OAuth consolidation details
- **[VERIFICATION_CHECKLIST.md](VERIFICATION_CHECKLIST.md)** - Testing and verification guide

## Recent Major Updates

### Enterprise Package Structure Standardization (v0.9.2)
- **Group ID Standardization**: Migrated from `press.mizhifei.dentist` to `com.dentistdss` across all 15+ microservices
- **Package Structure Refactoring**: Complete refactoring of all Java packages to follow enterprise naming conventions
- **Configuration Updates**: Updated all Spring configuration files, component scanning, and package references
- **Build System Updates**: Updated Gradle build files and dependency management configurations
- **Comprehensive Testing**: Verified compilation, testing, and functionality across all services
- **Benefits Achieved**:
  - **Enterprise Compliance**: Follows standard enterprise Java package naming conventions
  - **Improved Maintainability**: Consistent package structure across all microservices
  - **Better Organization**: Clear separation and organization of service components
  - **Professional Standards**: Aligns with industry best practices for microservices architecture
  - **Future-Proof**: Establishes foundation for enterprise-grade development and deployment

### System Service Refactoring to System Admin Service
- **Service Renaming**: Refactored `system-service` to `system-admin-service` for comprehensive system administration
- **Enhanced System Configuration Management**: Complete rate limiting and system parameter administration
- **Configuration Refresh Orchestration**: Centralized configuration refresh across all microservices
- **SYSTEM_ADMIN Role-Based Access Control**: Strict access control for system administration functions
- **SOLID Principles**: Complete redesign following SOLID principles and clean architecture patterns
- **New Features**:
  - **Rate Limiting Configuration**: Dynamic management of API rate limits with multi-dimensional limiting (per-user, per-role, per-clinic)
  - **System Parameter Management**: Comprehensive system-wide parameter configuration with validation and constraints
  - **Configuration Refresh Orchestration**: Centralized refresh management for all microservices with status tracking
  - **Audit and Monitoring**: Complete audit trail for all configuration changes and refresh operations
  - **API Endpoint Restructuring**: Updated from `/api/system/**` to `/api/system-admin/**` for better semantic clarity

### Clinic Service Refactoring to Clinic Admin Service
- **Service Renaming**: Refactored `clinic-service` to `clinic-admin-service` for focused clinic administration
- **Enhanced Functionality**: Added comprehensive working hours and holiday management capabilities
- **Role-Based Access Control**: Implemented proper authorization for clinic administrators
- **SOLID Principles**: Complete redesign following SOLID principles and clean architecture patterns
- **New Features**:
  - **Working Hours Management**: Full CRUD operations for clinic schedules with support for regular weekly hours and special date-specific hours
  - **Holiday Management**: Comprehensive holiday scheduling with recurring holidays, emergency closures, and special hours
  - **Enhanced Clinic CRUD**: Improved clinic management with time zone support and approval workflows
  - **API Endpoint Restructuring**: Updated from `/api/clinics/**` to `/api/clinic-admin/**` for better semantic clarity

### Spring Cloud Config Server Implementation
- **Externalized Configuration**: All configuration moved to external Git repository
- **12-Factor App Compliance**: Complete separation of configuration from code
- **Hot Reload Capabilities**: Configuration refresh without service restarts
- **Environment-Specific Configs**: Proper configuration hierarchy for dev/docker/prod

### Rate Limiting Refactoring
- **Centralized Rate Limiting**: Moved from service-level to API Gateway
- **Dynamic Configuration**: Rate limits configurable via system-service APIs
- **Role-Based Limiting**: Different limits for different user roles and clinics
- **Performance Optimization**: Distributed rate limiting with Redis support

### Reporting Service Integration
- **Advanced Analytics**: Comprehensive reporting with multi-format generation
- **Email Delivery**: Automated report delivery with professional formatting
- **Performance Optimization**: Async processing with caching and read replicas
- **Security Compliance**: HIPAA-compliant data handling and audit trails

### Circular Dependency Resolution (API Gateway)
- **Issue**: Resolved circular dependency in rate limiting system (RateLimitFilter → RateLimitService → RateLimitConfigResolver → RateLimitConfigClient → filteringWebHandler → RateLimitFilter)
- **Solution**: Replaced Feign client with direct RestTemplate service calls to system-admin-service
- **Benefits**: Eliminated dependency cycle, improved performance with direct HTTP calls, enhanced reliability with proper fallbacks
- **Implementation**: Created RateLimitConfigService with dedicated RestTemplate bean and proper timeout configurations

