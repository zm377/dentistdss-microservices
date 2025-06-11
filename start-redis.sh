#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting Redis service...${NC}"

# Check if Redis is already running
REDIS_RUNNING=$(docker ps -q -f name=redis)

if [ -n "$REDIS_RUNNING" ]; then
    echo -e "${GREEN}Redis container is already running.${NC}"
    exit 0
else
    echo -e "${YELLOW}Redis container is not running. Attempting to start it...${NC}"
fi

# Start Redis service
docker-compose up -d redis

# Verify
REDIS_AFTER_START=$(docker ps -q -f name=redis)

if [ -n "$REDIS_AFTER_START" ]; then
    echo -e "${GREEN}Redis container is now running.${NC}"
    echo -e "${GREEN}Redis is available at localhost:6379${NC}"
else
    echo -e "${RED}Failed to start Redis container.${NC}"
    exit 1
fi

echo -e "${GREEN}Redis startup script finished.${NC}"