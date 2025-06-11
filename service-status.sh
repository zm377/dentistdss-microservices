#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Checking status of all microservices...${NC}"
echo ""

services=("postgres" "mongo" "config-server" "discovery-server" "api-gateway" "auth-service" "audit-service" "system-admin-service" "genai-service" "clinic-admin-service" "appointment-service" "clinical-records-service" "user-profile-service" "admin-server" "notification-service" "chat-log-service" "reporting-service")

for service in "${services[@]}"; do
    status=$(docker-compose ps $service | grep -q "Up" && echo "Running" || echo "Stopped")
    
    if [ "$status" == "Running" ]; then
        echo -e "$service: ${GREEN}$status${NC}"
    else
        echo -e "$service: ${RED}$status${NC}"
    fi
done

echo ""
echo -e "${YELLOW}Service ports:${NC}"
echo -e "PostgreSQL: 5432"
echo -e "MongoDB: 27017"
echo -e "Config Server: 8888"
echo -e "Discovery Server: 8761"
echo -e "API Gateway: 443 (HTTPS)"
echo -e "Auth Service: 8081"
echo -e "Clinic Admin Service: 8083"
echo -e "GenAI Service: 8084"
echo -e "User Profile Service: 8085"
echo -e "System Admin Service: 8086"
echo -e "Audit Service: 8087"
echo -e "Notification Service: 8088"
echo -e "Appointment Service: 8089"
echo -e "Clinical Records Service: 8090"
echo -e "Chat Log Service: 8091"
echo -e "Reporting Service: 8092"
echo -e "Admin Server: 9090"