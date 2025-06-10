#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# List of services
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
  "user-profile-service"
  "admin-server"
  "notification-service"
)

printf "${YELLOW}Stopping all local Java services...${NC}\n"

for SERVICE in "${SERVICES[@]}"; do
  JAR_PATH=$(ls "$SERVICE"/target/*.jar 2>/dev/null | grep -v 'original-' | head -n 1)
  if [ -n "$JAR_PATH" ]; then
    PIDS=$(pgrep -f "java -jar $JAR_PATH")
    if [ -n "$PIDS" ]; then
      printf "${GREEN}Stopping $SERVICE (PID(s): $PIDS)...${NC}\n"
      kill $PIDS
    else
      printf "${YELLOW}No running process found for $SERVICE.${NC}\n"
    fi
  else
    printf "${YELLOW}No JAR found for $SERVICE, skipping.${NC}\n"
  fi
done

printf "${YELLOW}All stop commands issued. Check with 'ps aux | grep java' if needed.${NC}\n" 