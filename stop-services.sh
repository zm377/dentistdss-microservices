#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping all services except MySQL...${NC}"

# Stop only non-MySQL containers
docker-compose stop config-server discovery-server api-gateway auth-service oauth-service
docker-compose rm -f config-server discovery-server api-gateway auth-service oauth-service

echo -e "${GREEN}All services have been stopped. MySQL is still running to preserve data.${NC}"
echo -e "${YELLOW}To stop MySQL as well, run:${NC} docker-compose stop mysql" 