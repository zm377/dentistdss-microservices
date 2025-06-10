#!/bin/bash

# Docker environment startup script with environment auto-detection
# Usage: ./start-docker.sh [--dev|--prod] [--parallel|--sequential] [--max-parallel=N] [--skip-build]

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROFILE=""
BUILD_MODE="parallel"
MAX_PARALLEL=4
SKIP_BUILD=false

# Auto-detect environment if not specified
detect_environment() {
  if [[ -n "$DOCKER_ENV" ]]; then
    echo "$DOCKER_ENV"
  elif [[ "$(git branch --show-current 2>/dev/null)" == "main" ]] || [[ "$(git branch --show-current 2>/dev/null)" == "master" ]]; then
    echo "prod"
  else
    echo "docker"
  fi
}

# Parse arguments
for arg in "$@"; do
  case $arg in
    --dev|--development|docker)
      PROFILE="docker"
      ;;
    --prod|--production|prod)
      PROFILE="prod"
      ;;
    --parallel)
      BUILD_MODE="parallel"
      ;;
    --sequential)
      BUILD_MODE="sequential"
      ;;
    --max-parallel=*)
      MAX_PARALLEL="${arg#*=}"
      if ! [[ "$MAX_PARALLEL" =~ ^[0-9]+$ ]] || [ "$MAX_PARALLEL" -lt 1 ]; then
        echo -e "${RED}❌ Invalid max-parallel value: $MAX_PARALLEL${NC}"
        exit 1
      fi
      ;;
    --skip-build)
      SKIP_BUILD=true
      ;;
    *)
      if [[ "$arg" == "docker" ]] || [[ "$arg" == "prod" ]]; then
        PROFILE="$arg"
      else
        echo -e "${RED}❌ Unknown argument: $arg${NC}"
        echo "Usage: $0 [--dev|--prod] [--parallel|--sequential] [--max-parallel=N] [--skip-build]"
        exit 1
      fi
      ;;
  esac
done

# Set profile if not specified
[[ -z "$PROFILE" ]] && PROFILE=$(detect_environment)

# Validate profile
[[ "$PROFILE" != "docker" && "$PROFILE" != "prod" ]] && {
  echo -e "${RED}❌ Invalid profile: $PROFILE${NC}"
  exit 1
}

echo -e "${BLUE}🚀 Starting Docker environment${NC}"
echo -e "   Profile: $PROFILE | Build: $BUILD_MODE | Skip: $SKIP_BUILD"
[[ "$BUILD_MODE" == "parallel" ]] && echo -e "   Max Parallel: $MAX_PARALLEL"

# Build services
if [[ "$SKIP_BUILD" == "false" ]]; then
  echo -e "${YELLOW}Building with profile: $PROFILE${NC}"
  ./mvnw clean package -P"$PROFILE" -DskipTests || {
    echo -e "${RED}Build failed${NC}"
    exit 1
  }
fi

# Set compose file and build args
COMPOSE_FILE="docker-compose.yml"
[[ "$PROFILE" == "prod" ]] && COMPOSE_FILE="docker-compose.prod.yml"

COMPOSE_BUILD_ARGS=""
[[ "$BUILD_MODE" == "parallel" ]] && COMPOSE_BUILD_ARGS="--parallel"

# Database container detection
POSTGRES_RUNNING=$(docker ps -q -f name=postgres)
MONGO_RUNNING=$(docker ps -q -f name=mongo)

SERVICES="config-server discovery-server api-gateway auth-service audit-service system-service genai-service clinic-service appointment-service clinical-records-service user-profile-service admin-server notification-service"

if [[ -n "$POSTGRES_RUNNING" || -n "$MONGO_RUNNING" ]]; then
    echo -e "${GREEN}Preserving running databases${NC}"

    docker-compose -f "$COMPOSE_FILE" stop $SERVICES
    docker-compose -f "$COMPOSE_FILE" rm -f $SERVICES

    docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d $COMPOSE_BUILD_ARGS $SERVICES

    [[ -z "$MONGO_RUNNING" ]] && docker-compose -f "$COMPOSE_FILE" up -d mongo
else
    echo -e "${YELLOW}Starting all services${NC}"

    docker-compose -f "$COMPOSE_FILE" down
    docker-compose -f "$COMPOSE_FILE" rm -f

    if [[ "$BUILD_MODE" == "parallel" ]]; then
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d $COMPOSE_BUILD_ARGS
    else
      docker-compose -f "$COMPOSE_FILE" up --build --force-recreate -d $SERVICES mongo postgres
    fi
fi

echo -e "${GREEN}✅ Environment started: $PROFILE${NC}"

if [[ "$PROFILE" == "prod" ]]; then
  echo -e "${YELLOW}🔒 Production mode - OpenAPI disabled${NC}"
  echo -e "   Test: curl -k https://localhost:443/swagger-ui.html (should fail)"
else
  echo -e "${GREEN}🛠️ Development mode - OpenAPI enabled${NC}"
  echo -e "   Docs: https://localhost:443/swagger-ui.html"
fi

echo -e "${BLUE}Tips: --parallel (default) | --skip-build | --max-parallel=N${NC}"
echo ""
docker-compose -f "$COMPOSE_FILE" logs -f