# Backend - DentistDSS Microservices

This project is a microservices-based application for a Dentist Decision Support System.

Try on our website: [https://dentist.mizhifei.press/](https://dentist.mizhifei.press/)

This project codebase is managed by git on GitHub: [https://github.com/zm377/dentistdss-microservices](https://github.com/zm377/dentistdss-microservices)
## Services Architecture

The system consists of the following microservices:

### Databases

1. **PostgreSQL Database (v17)**
   - **Purpose**: Primary relational database for structured data storage
   - **Port**: 5432
   - **Database Name**: dentistdss
   - **Used by**: Auth Service, OAuth Service, Clinic Service, Patient Service, System Service
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

5. **API Gateway**
   - **Purpose**: Single entry point for all client requests with security
   - **Port**: 443 (HTTPS)
   - **Technology**: Spring Cloud Gateway
   - **Features**:
     - Routes requests to appropriate microservices
     - Implements TLS/SSL for secure HTTPS communication
     - Handles authentication and authorization
     - Provides rate limiting and request filtering
     - Enables CORS for frontend applications

### Core Business Services

6. **Auth Service**
   - **Purpose**: Handles user authentication and session management
   - **Port**: 8081
   - **Features**:
     - User registration with email verification
     - Login/logout functionality
     - Password management (reset, change)
     - JWT token generation and validation
     - Role-based access control (Patient, Dentist, Receptionist, Admin)

7. **OAuth Service**
   - **Purpose**: Provides OAuth2 authentication with external providers
   - **Port**: 8082
   - **Features**:
     - Google OAuth2 integration for social login
     - OAuth2 authorization server capabilities
     - Token management and refresh
     - Secure third-party authentication flow

8. **Clinic Service**
   - **Purpose**: Manages dental clinic operations and data
   - **Port**: 8083
   - **Features**:
     - Clinic registration and management
     - Staff management (Dentists, Receptionists)
     - Clinic profile and settings
     - Operating hours and availability
     - Service offerings and pricing

9. **Patient Service**
   - **Purpose**: Handles patient-related operations
   - **Port**: 8085
   - **Features**:
     - Patient profile management
     - Medical history tracking
     - Appointment booking and management
     - Treatment history and records
     - Patient-dentist communication

10. **System Service**
    - **Purpose**: Manages system-wide configurations and operations
    - **Port**: 8086
    - **Features**:
      - User role management and permissions
      - System-wide settings and preferences
      - User approval workflows for new registrations
      - System health monitoring
      - Administrative functions

### Communication Services

11. **Notification Service**
    - **Purpose**: Handles all notification and communication features
    - **Port**: 8088
    - **Features**:
      - Multi-channel notifications (Email, SMS, Push, In-App)
      - Template-based notification system with variable substitution
      - Scheduled notification delivery
      - Notification history and read status tracking
      - Email service integration with SMTP
      - Asynchronous notification processing
      - Integration with other services via Feign clients
      - Notification templates management

### AI and Analytics Services

12. **GenAI Service**
    - **Purpose**: Provides AI-powered chatbot and decision support features
    - **Port**: 8084
    - **Technology**: Spring AI with OpenAI integration
    - **Features**:
      - Three specialized AI chatbots:
        - Help Desk Bot (`/api/genai/chatbot/help`) - Public access for general inquiries
        - Receptionist Bot (`/api/genai/chatbot/receptionist`) - Appointment scheduling assistance
        - AI Dentist Bot (`/api/genai/chatbot/aidentist`) - Medical diagnosis and treatment guidance
      - Conversation history tracking in MongoDB
      - Rate limiting (10,000 tokens per 3 minutes per session)
      - Integration with patient records for personalized responses
      - Streaming API for real-time chat interactions

13. **Audit Service**
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

14. **Admin Server**
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
4. Auth Service
5. OAuth Service
6. Audit Service
7. System Service
8. GenAI Service
9. Clinic Service
10. Patient Service
11. Admin Server
12. Notification Service

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

# Production deployment
./build-and-push.sh     # Build and push latest images
./build-and-push.sh v1.2.3  # Build and push with version tag
./deploy.sh             # Deploy latest to production
./deploy.sh v1.2.3      # Deploy specific version
```

## Service Endpoints

### Public Endpoints
- **API Gateway**: https://api.mizhifei.press (Port 443 - HTTPS)
  - All microservices are accessible through the gateway with proper routing

### Infrastructure Services
- **Config Server**: http://localhost:8888
- **Discovery Server (Eureka)**: http://localhost:8761
  - Web UI available at http://localhost:8761/eureka

### Authentication Services
- **Auth Service**: http://localhost:8081
- **OAuth Service**: http://localhost:8082

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

## Security

The API Gateway is configured with TLS/SSL certificates for secure HTTPS communication. 
All internal service communication happens within a Docker network.

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