#!/bin/bash

# Deploy single microservice in production
# Usage: ./deploy-prod-single-service.sh <service-name> [version]

set -euo pipefail

# Config
REGISTRY_REPO="zm377/dentistdss-microservices"
SERVICES=(config-server discovery-server api-gateway auth-service audit-service system-service genai-service clinic-service appointment-service clinical-records-service user-profile-service admin-server notification-service)
PROJECT_NAME="dentistdss"
COMPOSE_FILE="docker-compose.prod.yml"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Parse arguments
SERVICE_NAME="${1:-}"
VERSION="${2:-latest}"

# Help
if [[ "$SERVICE_NAME" == "--help" || "$SERVICE_NAME" == "-h" ]]; then
  echo -e "${BLUE}Deploy Single Service - Production${NC}"
  echo "Usage: $0 <service-name> [version]"
  echo ""
  echo "Available services:"
  printf "  %s\n" "${SERVICES[@]}"
  echo ""
  echo "Examples:"
  echo "  $0 auth-service"
  echo "  $0 user-profile-service v1.2.3"
  exit 0
fi

# Validate service name
if [[ -z "$SERVICE_NAME" ]]; then
  echo -e "${RED}❌ Service name required${NC}"
  echo "Usage: $0 <service-name> [version]"
  echo "Use --help for more info"
  exit 1
fi

# Check if service exists
if [[ ! " ${SERVICES[*]} " =~ " $SERVICE_NAME " ]]; then
  echo -e "${RED}❌ Invalid service: $SERVICE_NAME${NC}"
  echo "Available services:"
  printf "  %s\n" "${SERVICES[@]}"
  exit 1
fi

# Check compose file
if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo -e "${RED}❌ $COMPOSE_FILE not found${NC}"
  exit 1
fi

echo -e "${BLUE}🚀 Deploying $SERVICE_NAME (production)${NC}"
echo "   Service: $SERVICE_NAME"
echo "   Version: $VERSION"
echo "   Environment: production"

# Image tags
REMOTE_TAG="$REGISTRY_REPO:${SERVICE_NAME}-${VERSION}"
LOCAL_TAG="${PROJECT_NAME}_${SERVICE_NAME}:latest"

# Check if image exists
echo -e "${YELLOW}🔍 Checking image availability...${NC}"
if ! docker manifest inspect "$REMOTE_TAG" >/dev/null 2>&1; then
  echo -e "${RED}❌ Image not found: $REMOTE_TAG${NC}"
  echo "Build it first with:"
  echo "  ./build-and-push-prod-single-service.sh $SERVICE_NAME $VERSION"
  exit 1
fi

echo -e "${GREEN}✅ Image found: $REMOTE_TAG${NC}"

# Pull and retag
echo -e "${YELLOW}📥 Pulling image...${NC}"
docker pull "$REMOTE_TAG"

echo -e "${YELLOW}🏷️  Retagging for compose...${NC}"
docker tag "$REMOTE_TAG" "$LOCAL_TAG"

# Deploy
echo -e "${YELLOW}🔄 Stopping existing container...${NC}"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" stop "$SERVICE_NAME" || true

echo -e "${YELLOW}🗑️  Removing old container...${NC}"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" rm -f "$SERVICE_NAME" || true

echo -e "${YELLOW}🚀 Starting updated container...${NC}"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" up -d --no-build "$SERVICE_NAME"

# Wait and verify
echo -e "${YELLOW}⏳ Waiting for service to start...${NC}"
sleep 15

# Check if container is running
if docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps "$SERVICE_NAME" | grep -q "Up"; then
  echo -e "${GREEN}✅ $SERVICE_NAME deployed successfully${NC}"
else
  echo -e "${RED}❌ $SERVICE_NAME failed to start${NC}"
  echo "Check logs with:"
  echo "  docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs $SERVICE_NAME"
  exit 1
fi

# Production verification for API Gateway
if [[ "$SERVICE_NAME" == "api-gateway" ]]; then
  echo -e "${YELLOW}🔍 Verifying OpenAPI is disabled...${NC}"
  sleep 10
  
  HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" "https://localhost:443/swagger-ui.html" || echo "000")
  if [[ "$HTTP_CODE" == "404" || "$HTTP_CODE" == "403" ]]; then
    echo -e "${GREEN}✅ OpenAPI properly disabled (HTTP $HTTP_CODE)${NC}"
  else
    echo -e "${YELLOW}⚠️  OpenAPI check returned HTTP $HTTP_CODE${NC}"
  fi
fi

echo ""
echo -e "${GREEN}🎉 Single service deployment complete!${NC}"
echo "📋 Summary:"
echo "   Service: $SERVICE_NAME"
echo "   Version: $VERSION"
echo "   Image: $REMOTE_TAG"
echo "   Status: Running"
echo ""
echo -e "${BLUE}🔗 Useful commands:${NC}"
echo "   Logs: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs $SERVICE_NAME"
echo "   Status: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME ps $SERVICE_NAME"
echo "   Restart: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME restart $SERVICE_NAME"
