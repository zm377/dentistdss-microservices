# DentistDSS Microservices

This project is a microservices-based application for a Dentist Decision Support System.

## Services Architecture

The system consists of the following microservices:

1. **MySQL Database** - Stores application data
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
2. Check if the MySQL container is already running
   - If running, it will preserve the MySQL container to maintain data
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

To stop all services except MySQL, run:

```bash
./stop-services.sh
```

This will preserve the MySQL container to maintain your valuable data.

If you need to stop MySQL as well, run:

```bash
docker-compose stop mysql
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
- **MySQL Database**: localhost:3306

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