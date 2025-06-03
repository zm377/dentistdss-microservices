#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Checking database container status...${NC}"

# Check if PostgreSQL container is running
POSTGRES_RUNNING=$(docker ps -q -f name=postgres)
# Check if MongoDB container is running
MONGO_RUNNING=$(docker ps -q -f name=mongo)

if [ -n "$POSTGRES_RUNNING" ] && [ -n "$MONGO_RUNNING" ]; then
    echo -e "${GREEN}Both PostgreSQL and MongoDB containers are already running.${NC}"
    exit 0
elif [ -n "$POSTGRES_RUNNING" ]; then
    echo -e "${GREEN}PostgreSQL container is already running.${NC}"
    echo -e "${YELLOW}MongoDB container is not running. Attempting to start it...${NC}"
elif [ -n "$MONGO_RUNNING" ]; then
    echo -e "${GREEN}MongoDB container is already running.${NC}"
    echo -e "${YELLOW}PostgreSQL container is not running. Attempting to start it...${NC}"
else
    echo -e "${YELLOW}Neither PostgreSQL nor MongoDB containers are running. Attempting to start both...${NC}"
fi

# Start database services
# docker-compose up will only start services that are not already running if they are specified.
docker-compose up -d postgres mongo

# Verify
POSTGRES_AFTER_START=$(docker ps -q -f name=postgres)
MONGO_AFTER_START=$(docker ps -q -f name=mongo)

echo "" # Newline for better readability

if [ -n "$POSTGRES_AFTER_START" ]; then
    echo -e "${GREEN}PostgreSQL container is now running.${NC}"
else
    echo -e "${RED}Failed to start PostgreSQL container or it was already stopped.${NC}"
fi

if [ -n "$MONGO_AFTER_START" ]; then
    echo -e "${GREEN}MongoDB container is now running.${NC}"
else
    echo -e "${RED}Failed to start MongoDB container or it was already stopped.${NC}"
fi

echo -e "${GREEN}Database startup script finished.${NC}"
