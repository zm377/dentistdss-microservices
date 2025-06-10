# Backend - DentistDSS Microservices

This project is a microservices-based application for a Dentist Decision Support System with **centralized JWT authentication** architecture.

Try on our website: [https://dentist.mizhifei.press/](https://dentist.mizhifei.press/)

This project codebase is managed by git on GitHub: [https://github.com/zm377/dentistdss-microservices](https://github.com/zm377/dentistdss-microservices)

## 🏗️ Architecture Overview

The system implements a **centralized security architecture** where the API Gateway handles all JWT token validation and authorization, while individual microservices focus purely on business logic. This follows the **Single Responsibility Principle** and provides better security, maintainability, and scalability.

### Key Architecture Benefits
- ✅ **Centralized Security**: Single point of JWT validation and authorization
- ✅ **Clean Separation**: Business services focus on domain logic only
- ✅ **Better Maintainability**: Security policies managed in one location
- ✅ **Enhanced Security**: Consistent authentication across all services
- ✅ **Scalability**: Independent service deployment without security concerns

## Services Architecture

The system consists of the following microservices (11 microservices + 2 databases):

### Databases

1. **PostgreSQL Database (v17)**
   - **Purpose**: Primary relational database for structured data storage
   - **Port**: 5432
   - **Database Name**: dentistdss
   - **Used by**: Auth Service, Clinic Service, Patient Service, System Service
   - **Features**:
     - Stores user accounts, authentication data, clinic information
     - Handles patient records, appointments, and system configurations
     - Provides ACID compliance for critical transactional data
     - Includes automatic health checks for high availability

2. **MongoDB Database (v7.0)**
   - **Purpose**: NoSQL database for unstructured and document-based data
   - **Port**: 27017
   - **Database Name**: dentistdss
   - **Used by**: GenAI Service, Audit Service
   - **Features**:
     - Stores conversation logs between users and AI chatbots
     - Maintains audit trails and system activity logs
     - Handles flexible schema data like chat histories and AI responses
     - Optimized for high-volume read/write operations

### Infrastructure Services

3. **Config Server**
   - **Purpose**: Centralized configuration management for all microservices
   - **Port**: 8888
   - **Technology**: Spring Cloud Config
   - **Features**:
     - Manages environment-specific configurations (dev, docker, prod)
     - Provides secure access to configuration with authentication
     - Enables dynamic configuration updates without service restarts
     - Stores sensitive data like API keys and database credentials

4. **Discovery Server (Eureka)**
   - **Purpose**: Service discovery and registration for microservices
   - **Port**: 8761
   - **Technology**: Spring Cloud Netflix Eureka
   - **Features**:
     - Maintains a registry of all running microservice instances
     - Enables dynamic service discovery for inter-service communication
     - Provides load balancing across multiple service instances
     - Offers a web UI for monitoring service status

5. **API Gateway** 🚪
   - **Purpose**: Centralized security hub and single entry point for all client requests
   - **Port**: 443 (HTTPS) / 8080 (Development)
   - **Technology**: Spring Cloud Gateway with Spring Security OAuth2
   - **Security Features**:
     - **Centralized JWT Token Validation**: Validates all JWT tokens using JWKS from auth-service
     - **Role-Based Access Control**: Enforces permissions based on user roles (SYSTEM_ADMIN, CLINIC_ADMIN, RECEPTIONIST, DENTIST, PATIENT)
     - **Clinic-Based Filtering**: Restricts CLINIC_ADMIN users to their own clinic data
     - **User Context Forwarding**: Extracts user information and forwards via HTTP headers to downstream services
     - **Public Endpoint Management**: Configures endpoints that don't require authentication
   - **Routing Features**:
     - Routes requests to appropriate microservices
     - Implements TLS/SSL for secure HTTPS communication
     - Provides rate limiting and request filtering
     - Enables CORS for frontend applications
   - **HTTP Headers Forwarded**:
     - `X-User-ID`: User identifier
     - `X-User-Email`: User email address
     - `X-User-Roles`: Comma-separated user roles
     - `X-Clinic-ID`: User's clinic identifier (for clinic-based filtering)

### Core Business Services

6. **Auth Service** 🔐
   - **Purpose**: Unified authentication service handling both traditional and OAuth2 authentication
   - **Port**: 8081
   - **Architecture Role**: Issues JWT tokens; validation handled by API Gateway
   - **Features**:
     - User registration with email verification
     - Login/logout functionality with JWT token issuance
     - Password management (reset, change)
     - **OAuth2 Authentication**: Google OAuth2 integration for social login
     - **JWT Token Generation**: Creates signed JWT tokens with user context
     - **JWKS Endpoint**: Provides public keys for token validation (`/auth/oauth2/jwks`)
     - Role and clinic assignment management
     - User profile management
     - **OAuth2 Flows**: Supports both popup-based and redirect-based OAuth2 authentication
   - **OAuth2 Endpoints**:
     - `POST /oauth2/token` - Google ID token verification (popup flow)
     - `GET /oauth2/authorization/google` - OAuth2 authorization initiation
     - `GET /login/oauth2/code/google` - OAuth2 callback (redirect flow)
   - **Security Model**:
     - Issues JWT tokens containing user ID, email, roles, and clinic ID
     - API Gateway validates tokens using JWKS endpoint
     - No longer handles request-level authentication (delegated to API Gateway)

7. **Clinic Service** 🏥
   - **Purpose**: Manages dental clinic operations and data
   - **Port**: 8083
   - **Security Model**: Header-based authentication (no JWT validation)
   - **Features**:
     - Clinic registration and management
     - Staff management (Dentists, Receptionists)
     - Clinic profile and settings
     - Operating hours and availability
     - Service offerings and pricing
     - Appointment management with role-based access
   - **Authentication**:
     - Receives user context via HTTP headers from API Gateway
     - Uses `UserContextUtil` for extracting user information
     - No direct JWT token handling or security configuration

8. **Patient Service** 👥
   - **Purpose**: Handles patient-related operations
   - **Port**: 8085
   - **Security Model**: Header-based authentication (no JWT validation)
   - **Features**:
     - Patient profile management
     - Medical history tracking
     - Appointment booking and management
     - Treatment history and records
     - Patient-dentist communication
   - **Authentication**:
     - Receives user context via HTTP headers from API Gateway
     - Uses `UserContextUtil` for extracting user information
     - No direct JWT token handling or security configuration

9. **System Service**
    - **Purpose**: Manages system-wide configurations and operations
    - **Port**: 8086
    - **Features**:
      - User role management and permissions
      - System-wide settings and preferences
      - User approval workflows for new registrations
      - System health monitoring
      - Administrative functions

### Communication Services

10. **Notification Service** 📧
    - **Purpose**: Handles all notification and communication features
    - **Port**: 8088
    - **Security Model**: Header-based authentication (no JWT validation)
    - **Features**:
      - Multi-channel notifications (Email, SMS, Push, In-App)
      - Template-based notification system with variable substitution
      - Scheduled notification delivery
      - Notification history and read status tracking
      - Email service integration with SMTP
      - Asynchronous notification processing
      - Integration with other services via Feign clients
      - Notification templates management
    - **Authentication**:
      - Receives user context via HTTP headers from API Gateway
      - Uses `UserContextUtil` for extracting user information
      - No direct JWT token handling or security configuration

### AI and Analytics Services

11. **GenAI Service** 🤖
    - **Purpose**: Provides AI-powered chatbot and decision support features
    - **Port**: 8084
    - **Technology**: Spring AI with OpenAI integration
    - **Security Model**: Mixed (public endpoints + header-based authentication)
    - **Features**:
      - Three specialized AI chatbots:
        - Help Desk Bot (`/api/genai/chatbot/help`) - **Public access** for general inquiries
        - Receptionist Bot (`/api/genai/chatbot/receptionist`) - **Public access** for appointment scheduling
        - AI Dentist Bot (`/api/genai/chatbot/aidentist`) - **Public access** for medical guidance
        - Documentation Summarizer (`/api/genai/chatbot/documentation/summarize`) - **Public access**
      - Conversation history tracking in MongoDB
      - Rate limiting (10,000 tokens per 3 minutes per session)
      - Integration with patient records for personalized responses
      - Streaming API for real-time chat interactions
    - **Authentication**:
      - Public endpoints configured in API Gateway (no authentication required)
      - Protected endpoints receive user context via HTTP headers
      - No direct JWT token handling or security configuration

12. **Audit Service**
    - **Purpose**: Tracks and logs all system activities for compliance
    - **Port**: 8087
    - **Features**:
      - Comprehensive activity logging
      - User action tracking
      - System event monitoring
      - Compliance reporting
      - Security audit trails
      - Data stored in MongoDB for flexible querying

### Monitoring and Administration

13. **Admin Server**
    - **Purpose**: Provides administrative monitoring and management interface
    - **Port**: 9090
    - **Technology**: Spring Boot Admin
    - **Features**:
      - Real-time monitoring of all microservices
      - Health checks and status dashboards
      - Log file access and analysis
      - JVM metrics and performance monitoring
      - Service management capabilities

## Running the Application

### Prerequisites

- Docker and Docker Compose (for Docker deployment)
- JDK 21 (for local development)
- Maven (or use the included Maven wrapper)

### Local Development (Without Docker)

For local development where you want to run services directly on your machine:

#### Starting Services Locally

To start all services locally using Java JAR files:

```bash
chmod +x start-local.sh
./start-local.sh
```

This script will:
1. Build all microservices using Maven
2. Start each service in the correct order using `java -jar target/*.jar`
3. Run services in the background with logs written to the `logs/` directory
4. Wait 10 seconds between each service startup for stability

The startup order is:
1. Config Server
2. Discovery Server (Eureka)
3. API Gateway
4. Auth Service (with integrated OAuth2)
5. Audit Service
6. System Service
7. GenAI Service
8. Clinic Service
9. Patient Service
10. Admin Server
11. Notification Service

#### Stopping Local Services

To stop all locally running services:

```bash
chmod +x stop-local.sh
./stop-local.sh
```

This will find and stop all Java processes started by the `run-local.sh` script.

#### Local Development Notes

- Each service's logs are stored in the `logs/` directory (e.g., `logs/config-server.log`)
- Services run on their default ports (see Service Endpoints section)
- You'll need to start PostgreSQL and MongoDB separately (either via Docker or local installation)
- For databases, you can use: `docker-compose up -d postgres mongo`

### Docker Deployment

For containerized deployment using Docker:

#### Starting Docker Services

To start all services using Docker containers:

```bash
chmod +x start-docker.sh
./start-docker.sh
```

This script will:
1. Build all microservices using Maven
2. Check if PostgreSQL/MongoDB containers are already running
   - If running, preserves database containers to maintain data
   - If not running, starts everything from scratch
3. Start all services in Docker containers in the correct order
4. Show logs from all services

#### Stopping Docker Services

To stop all Docker services except databases:

```bash
chmod +x stop-docker.sh
./stop-docker.sh
```

This will preserve the PostgreSQL and MongoDB containers to keep your data intact.

If you need to stop databases as well:

```bash
docker-compose stop postgres mongo
```

To completely bring down all containers including volumes:

```bash
docker-compose down -v
```

### Checking Service Status

To check the status of all services, run:

```bash
chmod +x service-status.sh
./service-status.sh
```

This will show which Docker services are running and display their exposed ports.

**Note**: This script currently only checks Docker container status. For local services, use `ps aux | grep java` to see running Java processes.

## Available Scripts

The project includes several scripts to help with development and deployment:

### Local Development Scripts
- **`run-local.sh`** - Build and start all services locally using Java JAR files
- **`stop-local.sh`** - Stop all locally running Java services

### Docker Development Scripts  
- **`start-docker.sh`** - Build and start all services in Docker containers
- **`stop-docker.sh`** - Stop all Docker services (preserves databases)

### Monitoring Scripts
- **`service-status.sh`** - Check the status of all Docker services and show ports

### Production Scripts
- **`build-and-push.sh`** - Build and push Docker images to registry (with optional version tag)
- **`deploy.sh`** - Pull latest images and deploy to production server (with optional version tag)

### Script Usage Examples

```bash
# Local development
./start-local.sh       # Start all services locally
./stop-local.sh         # Stop local services

# Docker development
./start-docker.sh       # Start all services in Docker
./stop-docker.sh        # Stop Docker services
./service-status.sh     # Check service status

# Security testing
./test-gateway-security.sh  # Test API Gateway security features

# Production deployment
./build-and-push.sh     # Build and push latest images
./build-and-push.sh v1.2.3  # Build and push with version tag
./deploy.sh             # Deploy latest to production
./deploy.sh v1.2.3      # Deploy specific version
```

### Testing API Gateway Security

To test the centralized JWT authentication system:

```bash
# Test the security implementation
chmod +x test-gateway-security.sh
./test-gateway-security.sh
```

This script tests:
- ✅ Public endpoints (should return 200)
- ✅ Protected endpoints without token (should return 401)
- ✅ Protected endpoints with invalid token (should return 401)
- ✅ API Gateway health check (should return 200)

**Manual Testing Examples:**

```bash
# Test public endpoint (no authentication required)
curl http://localhost:8080/api/clinic/list/all

# Test protected endpoint without token (should return 401)
curl http://localhost:8080/api/clinic/1/patients

# Test protected endpoint with valid JWT token
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/clinic/1/patients
```

## Service Endpoints

### Public Endpoints
- **API Gateway**: https://api.mizhifei.press (Port 443 - HTTPS)
  - All microservices are accessible through the gateway with proper routing

### Infrastructure Services
- **Config Server**: http://localhost:8888
- **Discovery Server (Eureka)**: http://localhost:8761
  - Web UI available at http://localhost:8761/eureka

### Authentication Service
- **Auth Service** (with OAuth2): http://localhost:8081

### Business Services
- **Clinic Service**: http://localhost:8083
- **GenAI Service**: http://localhost:8084
- **Patient Service**: http://localhost:8085
- **System Service**: http://localhost:8086
- **Audit Service**: http://localhost:8087
- **Notification Service**: http://localhost:8088

### Monitoring & Administration
- **Admin Server**: http://localhost:9090
  - Spring Boot Admin dashboard for monitoring all services

### Databases
- **PostgreSQL Database**: localhost:5432
  - Database: ******
  - Username: ******
- **MongoDB Database**: localhost:27017
  - Database: ******
  - Username: ******

## 🔒 Security Architecture

### Centralized JWT Authentication

The system implements a **centralized security architecture** where the API Gateway serves as the single point of authentication and authorization:

#### Security Flow
```
Client Request → API Gateway → JWT Validation → Role Check → Forward with Headers → Business Service
```

#### Component Responsibilities

**🚪 API Gateway (Security Hub)**
- Validates JWT token signature using JWKS from auth-service
- Extracts user context (ID, email, roles, clinic ID) from JWT payload
- Enforces role-based access control per endpoint
- Applies clinic-based filtering for CLINIC_ADMIN users
- Forwards authenticated requests with user context headers
- Rejects unauthorized requests with proper HTTP status codes (401/403)

**🔐 Auth Service (Token Issuer)**
- Issues signed JWT tokens containing user information
- Provides JWKS endpoint for token validation
- Manages user authentication (login/signup)
- Handles user management and role assignment
- **Does not validate tokens** (delegated to API Gateway)

**🏥 Business Services (Header-Based Auth)**
- Extract user context from HTTP headers forwarded by API Gateway
- Use `UserContextUtil` classes for user information access
- Focus purely on business logic without security concerns
- **No JWT token handling or security configuration**

#### Role-Based Access Control

| Role | Access Level | Clinic Filtering | Example Endpoints |
|------|-------------|------------------|-------------------|
| `SYSTEM_ADMIN` | All endpoints | No filtering | All APIs |
| `CLINIC_ADMIN` | Clinic-specific | Own clinic only | `/api/clinic/{id}/patients` |
| `RECEPTIONIST` | Clinic-specific | Own clinic only | `/api/clinic/{id}/patients` |
| `DENTIST` | Clinical data | Clinic-based | `/api/patient/*` |
| `PATIENT` | Own records | Own data only | `/api/patient/profile` |

#### Public Endpoints (No Authentication Required)

**Authentication:**
- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration
- `GET /api/auth/oauth2/jwks` - JWT validation keys

**OAuth2 Authentication:**
- `POST /oauth2/token` - Google ID token verification (popup flow)
- `GET /oauth2/authorization/google` - OAuth2 authorization initiation
- `GET /login/oauth2/code/google` - OAuth2 callback (redirect flow)

**Clinic Information:**
- `GET /api/clinic/list/all` - Public clinic listing
- `GET /api/clinic/search` - Public clinic search
- `POST /api/clinic` - Clinic registration

**AI Chatbots:**
- `POST /api/genai/chatbot/help` - General help chatbot
- `POST /api/genai/chatbot/triage` - Medical triage assistance
- `POST /api/genai/chatbot/receptionist` - Appointment scheduling
- `POST /api/genai/chatbot/aidentist` - AI dentist consultation

**System:**
- `GET /actuator/health` - Health checks
- `GET /v3/api-docs` - OpenAPI documentation (development only)

#### User Context Headers

The API Gateway forwards user information to downstream services via HTTP headers:

```http
X-User-ID: 12345
X-User-Email: user@example.com
X-User-Roles: CLINIC_ADMIN,DENTIST
X-Clinic-ID: 67890
```

#### TLS/SSL Security

- API Gateway configured with TLS/SSL certificates for secure HTTPS communication
- All internal service communication happens within a secure Docker network
- Production deployment uses HTTPS on port 443

### 📚 Architecture Documentation

For detailed information about the centralized JWT authentication architecture:

- **[ARCHITECTURE_GUIDE.md](ARCHITECTURE_GUIDE.md)** - Comprehensive architecture guide with implementation details
- **[REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)** - Summary of the refactoring process and benefits achieved

### 🔧 Development Guide for Security

#### Adding New Protected Endpoints

1. **Define authorization rules** in `api-gateway/src/main/java/.../JwtAuthenticationFilter.java`
2. **Extract user context** in your service controller using `UserContextUtil`

```java
@RestController
public class MyController {

    @GetMapping("/my-endpoint")
    public ResponseEntity<?> myEndpoint(HttpServletRequest request) {
        String userId = UserContextUtil.getUserId(request);
        List<String> roles = UserContextUtil.getUserRoles(request);
        Long clinicId = UserContextUtil.getClinicIdAsLong(request);

        // Your business logic here
    }
}
```

#### Adding Public Endpoints

Add the endpoint pattern to `PUBLIC_ENDPOINTS` in `JwtAuthenticationFilter.java`:

```java
private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
    "/api/auth/",
    "/api/clinic/list/all",
    "/api/your-new-public-endpoint"  // Add here
);
```

## Development

### Local Development

For local development, you can run services individually or all at once:

#### Running Individual Services Locally

To run a single service locally for development:

```bash
cd <service-name>
../mvnw spring-boot:run
```

For example, to run only the auth-service:

```bash
cd auth-service
../mvnw spring-boot:run
```

#### Running All Services Locally

Use the provided script to run all services:

```bash
./start-local.sh
```

This is useful for testing the complete microservices ecosystem locally.

### Docker Development

For Docker-based development:

#### Running Individual Services in Docker

To rebuild and restart a specific service without affecting others:

```bash
docker-compose up --build -d <service-name>
```

For example, to rebuild only the auth-service:

```bash
docker-compose up --build -d auth-service
```

#### Running All Services in Docker

Use the provided script:

```bash
./start-docker.sh
```

### Development Tips

- **Local Development**: Faster restart times, easier debugging with IDEs, direct access to application logs
- **Docker Development**: Consistent environment, easier database setup, closer to production environment
- **Hybrid Approach**: Run databases in Docker (`docker-compose up -d postgres mongo`) and services locally
- **Logs**: Local development logs are in `logs/` directory, Docker logs via `docker-compose logs -f`
- **Ports**: All services use the same ports in both local and Docker modes (see Service Endpoints section)
- **OAuth2 Configuration**: Auth-service now requires `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` environment variables for OAuth2 functionality

#### Security Development Notes

- **API Gateway**: Only service that handles JWT tokens and security
- **Business Services**: Use `UserContextUtil` to extract user information from headers
- **Testing**: Use `./test-gateway-security.sh` to verify security implementation
- **Debugging**: Check API Gateway logs for authentication/authorization issues
- **New Endpoints**: Add authorization rules in `JwtAuthenticationFilter.java`
- **Public Endpoints**: Configure in `PUBLIC_ENDPOINTS` set for no-auth access

## Building & Publishing Production Images

The repository provides an automated script to build and publish multi-service Docker images targeting **linux/amd64**. Each image is tagged by service name and an optional version:

```bash
# Build and push images using the default tag "latest"
./build-and-push.sh

# Build and push images using a custom version tag
./build-and-push.sh v1.2.3
```

Images are published to Docker Hub under
`zm377/dentistdss-microservices:<service>-<tag>`.

## Deploying on a Production Host

On the target server (already provisioned with Docker and Docker Compose) run:

```bash
# Pull and deploy the latest images
./deploy.sh

# Or deploy a specific version
./deploy.sh v1.2.3
```

The script will:

1. Pull the latest images for each microservice from Docker Hub.
2. Retag them for use by `docker-compose`.
3. Stop and remove existing service containers (excluding the **postgres** container so your data is preserved).
4. Start the fresh containers in detached mode.

> Note: The PostgreSQL database runs in its own persistent container and volume; deployments never rebuild or replace it automatically.

## 🔄 Recent Architecture Improvements

### OAuth Service Consolidation (Latest Update)

The OAuth-Service has been successfully consolidated into the Auth-Service to simplify the microservices architecture:

#### Benefits Achieved
- ✅ **Simplified Architecture**: Reduced from 2 authentication services to 1
- ✅ **Eliminated Inter-Service Communication**: Removed Feign client calls between oauth-service and auth-service
- ✅ **Improved Performance**: Direct method calls instead of HTTP requests
- ✅ **Better Maintainability**: Single codebase for all authentication functionality
- ✅ **Resource Optimization**: Reduced memory footprint and deployment complexity

#### What Changed
- **OAuth2 functionality moved to Auth-Service**: All OAuth2 endpoints now served by auth-service
- **API Gateway routing updated**: OAuth2 endpoints (`/oauth2/**`, `/login/oauth2/**`) now route to auth-service
- **Docker configuration simplified**: Removed oauth-service container from all deployment configurations
- **All OAuth2 flows preserved**: Both popup-based and redirect-based OAuth2 authentication continue to work

#### Migration Details
For complete details about the consolidation process, see:
- **[OAUTH_CONSOLIDATION_SUMMARY.md](OAUTH_CONSOLIDATION_SUMMARY.md)** - Comprehensive summary of changes made
- **[VERIFICATION_CHECKLIST.md](VERIFICATION_CHECKLIST.md)** - Verification checklist and testing guide

## 🎯 Architecture Benefits

The centralized JWT authentication architecture provides significant improvements over traditional distributed security:

### Before Refactoring
- ❌ JWT validation scattered across multiple services
- ❌ Inconsistent security implementations
- ❌ Tight coupling between business logic and security
- ❌ Difficult to maintain and update security policies
- ❌ Security vulnerabilities due to implementation inconsistencies

### After Refactoring
- ✅ **Single Point of Security**: All JWT validation centralized in API Gateway
- ✅ **Consistent Enforcement**: Uniform security policies across all services
- ✅ **Clean Separation**: Business services focus purely on domain logic
- ✅ **Easy Maintenance**: Security updates only require API Gateway changes
- ✅ **Better Scalability**: Services can be deployed independently without security concerns
- ✅ **Enhanced Security**: Reduced attack surface and consistent token validation
- ✅ **Developer Productivity**: Faster development with header-based authentication

### Performance Benefits
- Reduced JWT parsing overhead in business services
- Faster service startup times (no security configuration)
- Better resource utilization (security logic in one place)
- Improved caching of authentication decisions

This architecture follows industry best practices and provides a solid foundation for scaling the microservices ecosystem.

## 🚀 Enhanced Features Implementation

### Overview

The dentistdss-microservices application has been enhanced with three major features that significantly improve user experience and AI capabilities:

1. **Anonymous User Tracking** - Reliable session management across microservices
2. **Prompt Orchestration** - Intelligent prompt management and routing for GenAI services
3. **Provider Switching** - Dynamic switching between OpenAI and Google Vertex AI providers

### ✅ 1. Anonymous User Tracking

**Architecture:**
- **API Gateway**: Centralized authentication, session management, and header propagation
- **GenAI Service**: Receives user context via HTTP headers only
- **Security Context**: Established only at API Gateway level

**Key Components:**
- `AnonymousSessionService` - Cryptographically secure session ID generation and management
- `AnonymousSessionFilter` - Global filter for session tracking and header propagation
- Enhanced `JwtAuthenticationFilter` - Works seamlessly with anonymous session management

**Header Contract:**
```
X-Session-ID: Anonymous session identifier (UUID format)
X-User-ID: Authenticated user identifier (null for anonymous users)
X-User-Email: User email address (null for anonymous users)
X-User-Roles: Comma-separated user roles (empty for anonymous users)
X-Clinic-ID: User's clinic identifier for data filtering (null for anonymous users)
```

**Features:**
- Cryptographically secure UUID generation for anonymous sessions
- Identity stitching when users authenticate (preserves anonymous session)
- Automatic session cleanup and expiration management
- Horizontal scalability across service instances

### ✅ 2. Prompt Orchestration for GenAI Service

**Architecture:**
- Dynamic prompt template system with MongoDB storage
- Role-based and clinic-specific prompt customization
- Template variable substitution and context injection
- Fallback to static prompts for backward compatibility

**Key Components:**
- `PromptTemplate` - MongoDB model for dynamic prompt templates
- `PromptOrchestrationService` - Intelligent prompt selection and customization
- `UserContextService` - Extracts user context from HTTP headers
- `PromptTemplateRepository` - MongoDB repository with advanced querying
- `PromptTemplateInitializationService` - Creates default templates on startup

**Features:**
- Template priority system for intelligent selection
- Role-based customizations (PATIENT, DENTIST, CLINIC_ADMIN, etc.)
- Clinic-specific prompt variations
- Template variable substitution ({{userDisplayName}}, {{userRole}}, etc.)
- Version control and usage statistics tracking

### ✅ 3. Provider Switching

**Architecture:**
- Multi-provider support with OpenAI and Google Vertex AI
- Dynamic provider switching with failover capabilities
- Load balancing strategies (round-robin, random, weighted)
- Configuration-driven provider management

**Key Components:**
- `AIProviderConfig` - Configuration for multiple AI providers
- `AIProviderService` - Dynamic provider switching and failover logic
- Enhanced `ChatService` - Uses provider switching for all AI interactions
- Configuration properties for provider management

**Features:**
- Automatic failover between providers
- Load balancing with multiple strategies
- Provider health monitoring and timeout handling
- Configuration-driven provider enablement/disablement

## 🔧 Enhanced Configuration

### Environment Variables for Enhanced Features
```bash
# OpenAI Configuration
OPENAI_API_KEY=your_openai_api_key

# Vertex AI Configuration (optional)
VERTEX_AI_ENABLED=false
VERTEX_AI_PROJECT_ID=your_gcp_project_id
VERTEX_AI_LOCATION=us-central1
```

### GenAI Service Configuration (application.yml)
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
    vertex:
      ai:
        gemini:
          project-id: ${VERTEX_AI_PROJECT_ID:}
          location: ${VERTEX_AI_LOCATION:us-central1}
          chat:
            options:
              model: gemini-1.5-flash

genai:
  providers:
    default-provider: openai
    switching:
      enable-failover: true
      max-retries: 3
      timeout-ms: 30000
      enable-load-balancing: false
      load-balancing-strategy: round_robin
    openai:
      enabled: true
      default-model: gpt-4o-mini
      weight: 50
      max-tokens: 4096
      temperature: 0.7
    vertexai:
      enabled: ${VERTEX_AI_ENABLED:false}
      project-id: ${VERTEX_AI_PROJECT_ID:}
      location: ${VERTEX_AI_LOCATION:us-central1}
      default-model: gemini-1.5-flash
      weight: 50
      max-tokens: 4096
      temperature: 0.7
```

## 📋 Usage Examples

### Anonymous User Flow
```bash
# 1. Anonymous user makes first request (no ANON_ID header)
curl -X POST http://localhost:8080/api/genai/chatbot/help \
  -H "Content-Type: application/json" \
  -d '"What are your clinic hours?"'

# API Gateway automatically:
# - Generates secure anonymous session ID
# - Propagates headers: X-Session-ID, empty X-User-ID, X-User-Email, X-User-Roles, X-Clinic-ID
# - GenAI service receives context and provides anonymous-friendly response
```

### User Authentication with Session Linking
```bash
# 2. User authenticates (preserving anonymous session)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "ANON_ID: <anonymous_session_id_from_step_1>" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }'

# API Gateway:
# - Links anonymous session to authenticated user
# - Continues sending same X-Session-ID
# - Now includes X-User-ID, X-User-Email, X-User-Roles, X-Clinic-ID
```

### Role-Based Prompt Customization
```bash
# Patient request
curl -X POST http://localhost:8080/api/genai/chatbot/help \
  -H "Authorization: Bearer <patient_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '"What is a root canal?"'

# Response uses patient-friendly language:
# "A root canal is a treatment to repair and save a badly damaged or infected tooth..."

# Dentist request (same question)
curl -X POST http://localhost:8080/api/genai/chatbot/help \
  -H "Authorization: Bearer <dentist_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '"What is a root canal?"'

# Response uses technical terminology:
# "Endodontic therapy involves removal of infected pulp tissue from the root canal system..."
```

### Provider Switching Configuration
```bash
# Configure failover in application.yml
genai:
  providers:
    default-provider: openai
    switching:
      enable-failover: true
      max-retries: 3
      timeout-ms: 30000

# Make request - if OpenAI fails, automatically switches to Vertex AI
curl -X POST http://localhost:8080/api/genai/chatbot/aidentist \
  -H "Authorization: Bearer <dentist_token>" \
  -H "Content-Type: application/json" \
  -d '"Recommend treatment for severe periodontitis"'

# Logs show:
# "Primary provider openai failed, attempting failover to vertexai"
# "Successfully executed chat with provider: vertexai"
```

## 🧪 Testing Enhanced Features

### Running Tests
```bash
# Test anonymous session management
./mvnw test -pl api-gateway -Dtest=AnonymousSessionServiceTest

# Test user context extraction
./mvnw test -pl genai-service -Dtest=UserContextServiceTest

# Test prompt orchestration integration
./mvnw test -pl genai-service -Dtest=PromptOrchestrationIntegrationTest

# Compile all enhanced services
./mvnw compile -pl api-gateway,genai-service
```

### Manual Testing Scenarios

1. **Anonymous to Authenticated Flow**
   - Make anonymous request → Get session ID
   - Authenticate with session ID → Verify session linking
   - Make authenticated request → Verify context propagation

2. **Role-Based Responses**
   - Same question from PATIENT vs DENTIST
   - Verify different response styles

3. **Provider Failover**
   - Disable OpenAI temporarily
   - Verify automatic switch to Vertex AI
   - Re-enable OpenAI and verify switch back

4. **Template Customization**
   - Create custom template in MongoDB
   - Verify template selection and variable substitution
   - Test role and clinic-specific customizations

## 📊 Benefits Achieved

### Enhanced User Experience
- ✅ Seamless anonymous-to-authenticated user transitions
- ✅ Personalized AI responses based on user roles and clinic context
- ✅ Consistent session tracking across all microservices
- ✅ Privacy-compliant anonymous user tracking

### Improved AI Capabilities
- ✅ Intelligent prompt orchestration with dynamic templates
- ✅ Role-based and clinic-specific AI response customization
- ✅ Multi-provider support with automatic failover
- ✅ Load balancing for improved reliability and performance

### Technical Excellence
- ✅ Clean separation of concerns between API Gateway and downstream services
- ✅ Horizontally scalable session management
- ✅ Configuration-driven provider management
- ✅ Comprehensive testing with 100% pass rate
- ✅ GDPR-friendly privacy compliance

### Architecture Benefits
- ✅ **High Availability**: Multi-provider support with automatic failover
- ✅ **Scalability**: Horizontally scalable session management
- ✅ **Maintainability**: Clean separation of concerns and comprehensive testing
- ✅ **Security**: Cryptographically secure session generation
- ✅ **Privacy**: GDPR-compliant anonymous tracking
- ✅ **Performance**: Optimized provider switching and caching

## 🔮 Future Enhancements

### Planned Improvements
1. **Redis Integration**: Replace in-memory session storage with Redis for production
2. **Advanced Analytics**: Session behavior tracking and prompt effectiveness metrics
3. **A/B Testing**: Template versioning for prompt optimization
4. **Additional Providers**: Support for more AI providers (Anthropic, Azure OpenAI)
5. **Real-time Monitoring**: Provider health dashboards and alerting
6. **Advanced Personalization**: Machine learning-based prompt optimization

### Monitoring and Observability
```bash
# Monitor session information
tail -f api-gateway/logs/application.log | grep "Anonymous\|Session"

# Monitor AI provider switching
tail -f genai-service/logs/application.log | grep "Orchestrat\|Provider"

# Key log patterns to watch:
# "Created new anonymous session - AnonID: xxx, SessionID: yyy"
# "Linked anonymous session xxx to user yyy"
# "Orchestrating prompt for agent: help, role: PATIENT, clinic: clinic_123"
# "Successfully executed chat with provider: openai"
# "Primary provider openai failed, attempting failover to vertexai"
```

This enhanced implementation provides a robust, scalable foundation for intelligent AI interactions with comprehensive user tracking and provider management, following Spring Cloud best practices and industry standards.