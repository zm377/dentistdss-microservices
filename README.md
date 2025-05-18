# DentistDSS Microservices

This project is a microservices-based application for a Dentist Decision Support System.

## Services Architecture

The system consists of the following microservices:

1. **PostgreSQL Database (v17)** – Stores application data
2. **Config Server** - Centralized configuration service
3. **Discovery Server (Eureka)** - Service discovery and registration
4. **API Gateway** - Single entry point for clients with TLS/SSL
5. **Auth Service** - Handles authentication and authorization
6. **OAuth Service** - Provides OAuth2 authentication

## Running the Application

### Prerequisites

- Docker and Docker Compose
- JDK 21
- Maven (or use the included Maven wrapper)

### Starting the Application

To start all services, run:

```bash
./start-services.sh
```

This script will:
1. Build all microservices using Maven
2. Check if the PostgreSQL container is already running
   - If running, it will preserve the PostgreSQL container to maintain data
   - If not running, it will start everything from scratch
3. Start all services in the correct order
4. Show logs from all services

### Checking Service Status

To check the status of all services, run:

```bash
./service-status.sh
```

This will show which services are running and display their exposed ports.

### Stopping Services

To stop all services except PostgreSQL, run:

```bash
./stop-services.sh
```

This will preserve the PostgreSQL container to keep your data intact.

If you need to stop PostgreSQL as well, run:

```bash
docker compose stop postgres
```

To completely bring down all containers including volumes:

```bash
docker-compose down -v
```

## Service Endpoints

- **API Gateway**: https://api.mizhifei.press (Port 443 - HTTPS)
- **Auth Service**: http://localhost:8081
- **OAuth Service**: http://localhost:8082
- **Discovery Server**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **PostgreSQL Database**: localhost:5432

## Security

The API Gateway is configured with TLS/SSL certificates for secure HTTPS communication. 
All internal service communication happens within a Docker network.

## Development

To rebuild and restart a specific service without affecting others:

```bash
docker-compose up --build -d <service-name>
```

For example, to rebuild only the auth-service:

```bash
docker-compose up --build -d auth-service
```

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