#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# List of services in startup order
SERVICES=(
  "config-server"
  "discovery-server"
  "api-gateway"
  "auth-service"
  "audit-service"
  "system-service"
  "genai-service"
  "clinic-service"
  "appointment-service"
  "clinical-records-service"
  "user-profile-service"
  "admin-server"
  "notification-service"
)

# Corresponding log file names
LOGS=(
  "logs/config-server.log"
  "logs/discovery-server.log"
  "logs/api-gateway.log"
  "logs/auth-service.log"
  "logs/audit-service.log"
  "logs/system-service.log"
  "logs/genai-service.log"
  "logs/clinic-service.log"
  "logs/appointment-service.log"
  "logs/clinical-records-service.log"
  "logs/user-profile-service.log"
  "logs/admin-server.log"
  "logs/notification-service.log"
)

# Create logs directory if it doesn't exist
mkdir -p logs

# Build all services
printf "${YELLOW}Building all services...${NC}\n"
./mvnw clean package -DskipTests
if [ $? -ne 0 ]; then
  printf "${RED}Build failed. Exiting.${NC}\n"
  exit 1
fi

# Start each service in order
for i in "${!SERVICES[@]}"; do
  SERVICE="${SERVICES[$i]}"
  LOG="${LOGS[$i]}"
  printf "${GREEN}Starting $SERVICE...${NC}\n"
  (
    cd "$SERVICE" || { printf "${RED}Failed to cd into $SERVICE. Skipping.${NC}\n"; exit 0; }
    JAR_FILE=$(ls target/*.jar 2>/dev/null | grep -v 'original-' | head -n 1)
    if [ -z "$JAR_FILE" ]; then
      printf "${RED}No JAR found for $SERVICE. Skipping.${NC}\n"
      exit 0
    fi
    nohup java -jar "$JAR_FILE" > "../$LOG" 2>&1 &
  )
  sleep 10 # Wait for service to start (adjust as needed)
done

printf "${YELLOW}All services started. Logs are in the logs directory.${NC}\n" 