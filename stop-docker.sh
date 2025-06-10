#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping all services except PostgreSQL and MongoDB...${NC}"

# Stop only non-database containers
docker-compose stop config-server discovery-server api-gateway auth-service audit-service system-service genai-service clinic-service appointment-service user-profile-service admin-server notification-service
docker-compose rm -f config-server discovery-server api-gateway auth-service audit-service system-service genai-service clinic-service appointment-service user-profile-service admin-server notification-service

echo -e "${GREEN}All services have been stopped. PostgreSQL and MongoDB are still running to preserve data.${NC}"
echo -e "${YELLOW}To stop the databases as well, run:${NC} docker-compose stop postgres mongo" 