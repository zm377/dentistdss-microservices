#!/bin/bash

# Start Docker development environment with OpenAPI enabled and parallel building support
# Usage: ./start-docker.sh [profile] [--parallel|--sequential] [--max-parallel=N] [--skip-build]
# If no profile provided, "docker" is used (development with OpenAPI enabled)
# Use "prod" for production-like testing without OpenAPI
# Use --parallel for faster Docker builds (default), --sequential for debugging
# Use --max-parallel=N to limit concurrent builds (default: 4)
# Use --skip-build to skip Maven build and use existing JARs

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROFILE="${1:-docker}"
BUILD_MODE="parallel"
MAX_PARALLEL=4
SKIP_BUILD=false

# Parse additional arguments
for arg in "${@:2}"; do
  case $arg in
    --parallel)
      BUILD_MODE="parallel"
      ;;
    --sequential)
      BUILD_MODE="sequential"
      ;;
    --max-parallel=*)
      MAX_PARALLEL="${arg#*=}"
      if ! [[ "$MAX_PARALLEL" =~ ^[0-9]+$ ]] || [ "$MAX_PARALLEL" -lt 1 ]; then
        echo -e "${RED}❌ Invalid max-parallel value: $MAX_PARALLEL. Must be a positive integer.${NC}"
        exit 1
      fi
      ;;
    --skip-build)
      SKIP_BUILD=true
      ;;
    *)
      echo -e "${RED}❌ Unknown argument: $arg${NC}"
      echo "Usage: $0 [profile] [--parallel|--sequential] [--max-parallel=N] [--skip-build]"
      exit 1
      ;;
  esac
done

# Validate profile
if [[ "$PROFILE" != "docker" && "$PROFILE" != "prod" ]]; then
  echo -e "${RED}❌ Invalid profile: $PROFILE. Use 'docker' or 'prod'${NC}"
  exit 1
fi

echo -e "${BLUE}🚀 Starting Docker environment with optimized building${NC}"
echo -e "${YELLOW}📋 Configuration:${NC}"
echo -e "   Profile: $PROFILE"
echo -e "   Build Mode: $BUILD_MODE"
if [[ "$BUILD_MODE" == "parallel" ]]; then
  echo -e "   Max Parallel: $MAX_PARALLEL"
fi
echo -e "   Skip Build: $SKIP_BUILD"

if [[ "$SKIP_BUILD" == "false" ]]; then
  echo -e "${YELLOW}Building microservices packages with profile: $PROFILE...${NC}"
  if [[ "$PROFILE" == "prod" ]]; then
    echo -e "${YELLOW}🔒 Production build: SpringDoc OpenAPI will be excluded${NC}"
  else
    echo -e "${YELLOW}🛠️  Development build: SpringDoc OpenAPI will be included${NC}"
  fi

  ./mvnw clean package -P"$PROFILE" -DskipTests
else
  echo -e "${YELLOW}⏭️  Skipping Maven build, using existing JARs...${NC}"
fi

# Check if build was successful (only if we ran the build)
if [[ "$SKIP_BUILD" == "false" ]] && [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Exiting.${NC}"
    exit 1
fi

# Choose compose file based on profile
if [[ "$PROFILE" == "prod" ]]; then
  COMPOSE_FILE="docker-compose.prod.yml"
  echo -e "${YELLOW}🔒 Using production configuration (OpenAPI disabled)${NC}"
else
  COMPOSE_FILE="docker-compose.yml"
  echo -e "${YELLOW}🛠️  Using development configuration (OpenAPI enabled)${NC}"
fi

# Set Docker Compose parallel build options
COMPOSE_BUILD_ARGS=""
if [[ "$BUILD_MODE" == "parallel" ]]; then
  COMPOSE_BUILD_ARGS="--parallel"
  echo -e "${BLUE}⚡ Using Docker Compose parallel building${NC}"
fi

# Check if PostgreSQL container is running
POSTGRES_RUNNING=$(docker ps -q -f name=postgres)
# Check if MongoDB container is running
MONGO_RUNNING=$(docker ps -q -f name=mongo)

if [ -n "$POSTGRES_RUNNING" ] || [ -n "$MONGO_RUNNING" ]; then
    echo -e "${GREEN}Database containers are already running (PostgreSQL and/or MongoDB). Preserving them to maintain data.${NC}"

    # Stop only non-database containers
    echo -e "${YELLOW}Stopping existing non-database containers...${NC}"
    docker-compose -f "$COMPOSE_FILE" stop config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service

    echo -e "${YELLOW}Removing stopped containers...${NC}"
    docker-compose -f "$COMPOSE_FILE" rm -f config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service

    # Start non-database services with optimized building
    echo -e "${GREEN}Starting all non-database services with optimized builds...${NC}"
    if [[ "$BUILD_MODE" == "parallel" ]]; then
      echo -e "${BLUE}🚀 Building services in parallel for faster startup...${NC}"
      # Use Docker Compose's built-in parallel building
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d $COMPOSE_BUILD_ARGS config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service
    else
      echo -e "${YELLOW}🔄 Building services sequentially...${NC}"
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service
    fi

    # If MongoDB was not running, start it now (safe – will be a no-op if already up)
    if [ -z "$MONGO_RUNNING" ]; then
        echo -e "${YELLOW}MongoDB container is not running. Starting it now...${NC}"
        docker-compose -f "$COMPOSE_FILE" up -d mongo
    fi
else
    echo -e "${YELLOW}Database containers are not running. Starting all services...${NC}"

    # Stop all containers if any are running
    echo -e "${YELLOW}Stopping existing containers (if any)...${NC}"
    docker-compose -f "$COMPOSE_FILE" down

    echo -e "${YELLOW}Removing old containers to ensure clean state...${NC}"
    docker-compose -f "$COMPOSE_FILE" rm -f

    echo -e "${GREEN}Starting all services with optimized builds...${NC}"
    if [[ "$BUILD_MODE" == "parallel" ]]; then
      echo -e "${BLUE}🚀 Building and starting all services in parallel...${NC}"
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d $COMPOSE_BUILD_ARGS
    else
      echo -e "${YELLOW}🔄 Building and starting services sequentially...${NC}"
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service mongo postgres
    fi
fi

echo -e "${YELLOW}Services are starting in the following order:${NC}"
echo -e "1. PostgreSQL Database (preserved if already running)"
echo -e "2. MongoDB Database (preserved if already running)"
echo -e "3. Config Server"
echo -e "4. Discovery Server (Eureka)"
echo -e "5. API Gateway"
echo -e "6. Auth Service"
echo -e "7. OAuth Service"
echo -e "8. Audit Service"
echo -e "9. System Service"
echo -e "10. GenAI Service"
echo -e "11. Clinic Service"
echo -e "12. Patient Service"
echo -e "13. Admin Server"
echo -e "14. Notification Service"

# Post-startup information
echo ""
echo -e "${GREEN}✅ Docker environment startup completed!${NC}"
echo -e "${BLUE}📋 Startup Summary:${NC}"
echo -e "   Profile: $PROFILE"
echo -e "   Build Mode: $BUILD_MODE"
if [[ "$BUILD_MODE" == "parallel" ]]; then
  echo -e "   Parallel Building: Enabled"
fi
echo -e "   Compose File: $COMPOSE_FILE"

if [[ "$PROFILE" == "prod" ]]; then
  echo -e "${YELLOW}🔍 Production profile verification:${NC}"
  echo -e "${YELLOW}   - OpenAPI endpoints should return 404/403${NC}"
  echo -e "${YELLOW}   - SpringDoc dependencies should be excluded from JARs${NC}"
  echo -e "${YELLOW}   - Test with: curl -k https://localhost:443/swagger-ui.html${NC}"
else
  echo -e "${GREEN}🛠️  Development environment started with OpenAPI enabled${NC}"
  echo -e "${GREEN}   📚 API Documentation: https://localhost:443/swagger-ui.html${NC}"
fi

echo ""
echo -e "${BLUE}🎯 Performance Tips for future runs:${NC}"
echo -e "   • Use --parallel for faster Docker builds (default)"
echo -e "   • Use --skip-build to skip Maven build if JARs are current"
echo -e "   • Use --max-parallel=N to adjust concurrent builds"
echo -e "   • Use --sequential for debugging build issues"

echo ""
echo -e "${GREEN}Showing logs...${NC}"
docker-compose -f "$COMPOSE_FILE" logs -f