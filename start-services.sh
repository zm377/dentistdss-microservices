#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Building microservices packages...${NC}"
./mvnw clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Exiting.${NC}"
    exit 1
fi

# Check if PostgreSQL container is running
POSTGRES_RUNNING=$(docker ps -q -f name=postgres)
if [ -n "$POSTGRES_RUNNING" ]; then
    echo -e "${GREEN}PostgreSQL container is already running. Preserving it to maintain data.${NC}"
    
    # Stop only non-PostgreSQL containers
    echo -e "${YELLOW}Stopping existing non-PostgreSQL containers...${NC}"
    docker-compose stop config-server discovery-server api-gateway auth-service oauth-service
    docker-compose rm -f config-server discovery-server api-gateway auth-service oauth-service
    
    # Start non-PostgreSQL services
    echo -e "${GREEN}Starting all non-PostgreSQL services...${NC}"
    docker-compose up --build --force-recreate -d config-server discovery-server api-gateway auth-service oauth-service
else
    echo -e "${YELLOW}PostgreSQL container is not running. Starting all services...${NC}"
    
    # Stop all containers if any are running
    echo -e "${YELLOW}Stopping existing containers (if any)...${NC}"
    docker-compose down
    
    echo -e "${YELLOW}Removing old containers to ensure clean state...${NC}"
    docker-compose rm -f
    
    echo -e "${GREEN}Starting all services...${NC}"
    docker-compose up --build --force-recreate -d
fi

echo -e "${YELLOW}Services are starting in the following order:${NC}"
echo -e "1. PostgreSQL Database (preserved if already running)"
echo -e "2. Config Server"
echo -e "3. Discovery Server (Eureka)"
echo -e "4. API Gateway"
echo -e "5. Auth Service"
echo -e "6. OAuth Service"

echo -e "${GREEN}Showing logs...${NC}"
docker-compose logs -f 