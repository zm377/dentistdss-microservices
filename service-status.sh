#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Checking status of all microservices...${NC}"
echo ""

services=("postgres" "config-server" "discovery-server" "api-gateway" "auth-service" "oauth-service")

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
echo -e "Config Server: 8888"
echo -e "Discovery Server: 8761"
echo -e "API Gateway: 443 (HTTPS)"
echo -e "Auth Service: 8081"
echo -e "OAuth Service: 8082" 