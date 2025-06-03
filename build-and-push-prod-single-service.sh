#!/bin/bash

# Build and push a single microservice with production profile and optimizations
# Usage: ./build-and-push-prod-single-service.sh <service-name> [version] [profile] [options]
# 
# Parameters:
#   service-name: Required - Name of the service to build (e.g., auth-service, patient-service)
#   version: Optional - Image version tag (default: "latest")
#   profile: Optional - Maven profile: "dev", "docker", or "prod" (default: "prod")
#
# Options:
#   --parallel: Enable parallel optimizations (default)
#   --sequential: Disable parallel optimizations
#   --help: Show this help message
#   --list-services: List all available services
#
# Examples:
#   ./build-and-push-prod-single-service.sh auth-service
#   ./build-and-push-prod-single-service.sh patient-service v1.2.3
#   ./build-and-push-prod-single-service.sh genai-service latest docker
#   ./build-and-push-prod-single-service.sh clinic-service v2.0.0 prod --sequential

set -euo pipefail

# ---------- CONFIG ----------
REGISTRY_REPO="zm377/dentistdss-microservices"
PLATFORM="linux/amd64"
BUILDX_BUILDER="dentist-multiarch"

# All available services
ALL_SERVICES=(config-server discovery-server api-gateway auth-service oauth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service)

# Default values
SERVICE_NAME=""
VERSION="latest"
PROFILE="prod"
BUILD_MODE="parallel"
SHOW_HELP=false
LIST_SERVICES=false

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ---------- FUNCTIONS ----------

show_help() {
  echo -e "${BLUE}🔧 Single Service Build and Push Script${NC}"
  echo "========================================"
  echo ""
  echo -e "${YELLOW}USAGE:${NC}"
  echo "  $0 <service-name> [version] [profile] [options]"
  echo ""
  echo -e "${YELLOW}PARAMETERS:${NC}"
  echo "  service-name    Required - Name of the service to build"
  echo "  version         Optional - Image version tag (default: 'latest')"
  echo "  profile         Optional - Maven profile: 'dev', 'docker', or 'prod' (default: 'prod')"
  echo ""
  echo -e "${YELLOW}OPTIONS:${NC}"
  echo "  --parallel      Enable parallel optimizations (default)"
  echo "  --sequential    Disable parallel optimizations"
  echo "  --help          Show this help message"
  echo "  --list-services List all available services"
  echo ""
  echo -e "${YELLOW}EXAMPLES:${NC}"
  echo "  $0 auth-service"
  echo "  $0 patient-service v1.2.3"
  echo "  $0 genai-service latest docker"
  echo "  $0 clinic-service v2.0.0 prod --sequential"
  echo ""
  echo -e "${YELLOW}AVAILABLE SERVICES:${NC}"
  for svc in "${ALL_SERVICES[@]}"; do
    echo "  • $svc"
  done
  echo ""
  echo -e "${CYAN}💡 TIP: Use --list-services to see all available services${NC}"
}

list_services() {
  echo -e "${BLUE}📋 Available Services${NC}"
  echo "===================="
  echo ""
  for i in "${!ALL_SERVICES[@]}"; do
    local svc="${ALL_SERVICES[$i]}"
    local num=$((i + 1))
    if [[ -d "$svc" ]]; then
      echo -e "${GREEN}✅ $num. $svc${NC}"
    else
      echo -e "${RED}❌ $num. $svc (directory not found)${NC}"
    fi
  done
  echo ""
  echo -e "${YELLOW}Total services: ${#ALL_SERVICES[@]}${NC}"
}

validate_service() {
  local service="$1"
  
  # Check if service is in the list
  local found=false
  for svc in "${ALL_SERVICES[@]}"; do
    if [[ "$svc" == "$service" ]]; then
      found=true
      break
    fi
  done
  
  if [[ "$found" == "false" ]]; then
    echo -e "${RED}❌ ERROR: Invalid service name: '$service'${NC}"
    echo ""
    echo -e "${YELLOW}Available services:${NC}"
    for svc in "${ALL_SERVICES[@]}"; do
      echo "  • $svc"
    done
    echo ""
    echo -e "${CYAN}💡 Use --list-services to see all available services${NC}"
    return 1
  fi
  
  # Check if service directory exists
  if [[ ! -d "$service" ]]; then
    echo -e "${RED}❌ ERROR: Service directory not found: '$service'${NC}"
    echo "   Expected directory: ./$service"
    return 1
  fi
  
  # Check if service has a pom.xml
  if [[ ! -f "$service/pom.xml" ]]; then
    echo -e "${RED}❌ ERROR: Maven pom.xml not found for service: '$service'${NC}"
    echo "   Expected file: ./$service/pom.xml"
    return 1
  fi
  
  # Check if service has a Dockerfile
  if [[ ! -f "$service/Dockerfile" ]]; then
    echo -e "${RED}❌ ERROR: Dockerfile not found for service: '$service'${NC}"
    echo "   Expected file: ./$service/Dockerfile"
    return 1
  fi
  
  return 0
}

validate_profile() {
  local profile="$1"
  if [[ "$profile" != "dev" && "$profile" != "docker" && "$profile" != "prod" ]]; then
    echo -e "${RED}❌ ERROR: Invalid profile: '$profile'${NC}"
    echo "   Valid profiles: dev, docker, prod"
    return 1
  fi
  return 0
}

# ---------- ARGUMENT PARSING ----------

# Check if no arguments provided
if [[ $# -eq 0 ]]; then
  echo -e "${RED}❌ ERROR: Service name is required${NC}"
  echo ""
  show_help
  exit 1
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --help)
      SHOW_HELP=true
      shift
      ;;
    --list-services)
      LIST_SERVICES=true
      shift
      ;;
    --parallel)
      BUILD_MODE="parallel"
      shift
      ;;
    --sequential)
      BUILD_MODE="sequential"
      shift
      ;;
    -*)
      echo -e "${RED}❌ ERROR: Unknown option: $1${NC}"
      echo ""
      show_help
      exit 1
      ;;
    *)
      if [[ -z "$SERVICE_NAME" ]]; then
        SERVICE_NAME="$1"
      elif [[ "$VERSION" == "latest" ]]; then
        VERSION="$1"
      elif [[ "$PROFILE" == "prod" ]]; then
        PROFILE="$1"
      else
        echo -e "${RED}❌ ERROR: Too many positional arguments: $1${NC}"
        echo ""
        show_help
        exit 1
      fi
      shift
      ;;
  esac
done

# Handle help and list-services
if [[ "$SHOW_HELP" == "true" ]]; then
  show_help
  exit 0
fi

if [[ "$LIST_SERVICES" == "true" ]]; then
  list_services
  exit 0
fi

# Validate required service name
if [[ -z "$SERVICE_NAME" ]]; then
  echo -e "${RED}❌ ERROR: Service name is required${NC}"
  echo ""
  show_help
  exit 1
fi

# ---------- VALIDATION ----------

echo -e "${BLUE}🔍 Validating inputs...${NC}"

# Validate service name
if ! validate_service "$SERVICE_NAME"; then
  exit 1
fi

# Validate profile
if ! validate_profile "$PROFILE"; then
  exit 1
fi

echo -e "${GREEN}✅ All validations passed${NC}"

# ---------- BUILD CONFIGURATION ----------

echo ""
echo -e "${BLUE}🔧 Single Service Build Configuration${NC}"
echo "====================================="
echo -e "   Service: ${CYAN}$SERVICE_NAME${NC}"
echo -e "   Version: ${CYAN}$VERSION${NC}"
echo -e "   Profile: ${CYAN}$PROFILE${NC}"
echo -e "   Build Mode: ${CYAN}$BUILD_MODE${NC}"
echo -e "   Platform: ${CYAN}$PLATFORM${NC}"
echo -e "   Registry: ${CYAN}$REGISTRY_REPO${NC}"

if [[ "$PROFILE" == "prod" ]]; then
  echo -e "   ${YELLOW}🔒 Production build: SpringDoc OpenAPI dependencies will be excluded${NC}"
else
  echo -e "   ${YELLOW}🛠️  Development build: SpringDoc OpenAPI dependencies will be included${NC}"
fi

# ---------- BUILD SETUP ----------

# Global variables for build tracking
BUILD_LOG_DIR="/tmp/dentist-single-build-logs-$$"
BUILD_SUCCESS=false

# Cleanup function
cleanup() {
  echo ""
  echo -e "${YELLOW}🧹 Cleaning up build processes...${NC}"
  rm -rf "$BUILD_LOG_DIR" 2>/dev/null || true
}

# Set up cleanup trap
trap cleanup EXIT INT TERM

# Create log directory
mkdir -p "$BUILD_LOG_DIR"

# ---------- DOCKER BUILDX SETUP ----------

echo ""
echo -e "${YELLOW}🔧 Setting up Docker BuildX...${NC}"

# Ensure buildx builder exists
if ! docker buildx inspect "$BUILDX_BUILDER" >/dev/null 2>&1; then
  echo -e "${YELLOW}Creating buildx builder $BUILDX_BUILDER (first-time setup)...${NC}"
  docker buildx create --name "$BUILDX_BUILDER" --use
fi

echo -e "${YELLOW}Bootstrapping builder ($BUILDX_BUILDER)...${NC}"
docker buildx inspect --bootstrap "$BUILDX_BUILDER" >/dev/null

echo -e "${GREEN}✅ Docker BuildX ready${NC}"

# ---------- MAVEN BUILD ----------

echo ""
echo -e "${YELLOW}🔨 Building Maven module for $SERVICE_NAME with profile: $PROFILE...${NC}"

# Build only the specific service module
MAVEN_LOG="$BUILD_LOG_DIR/maven-build.log"

if ./mvnw -q clean package -P"$PROFILE" -DskipTests -pl "$SERVICE_NAME" -am > "$MAVEN_LOG" 2>&1; then
  echo -e "${GREEN}✅ Maven build completed successfully${NC}"
else
  echo -e "${RED}❌ Maven build failed${NC}"
  echo ""
  echo -e "${YELLOW}📋 Last 15 lines of Maven build log:${NC}"
  tail -n 15 "$MAVEN_LOG" | sed 's/^/   /'
  echo ""
  echo -e "${CYAN}💡 Full Maven log available at: $MAVEN_LOG${NC}"
  exit 1
fi

# ---------- JAR VERIFICATION ----------

echo ""
echo -e "${YELLOW}🔍 Verifying JAR file...${NC}"

# Find the built JAR file
JAR_PATTERN="./${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar"
JAR_FILES=($(ls $JAR_PATTERN 2>/dev/null | grep -v 'original-' || true))

if [[ ${#JAR_FILES[@]} -eq 0 ]]; then
  echo -e "${RED}❌ ERROR: No JAR file found for $SERVICE_NAME${NC}"
  echo "   Expected pattern: $JAR_PATTERN"
  echo "   Make sure the Maven build completed successfully."
  exit 1
fi

JAR_FILE="${JAR_FILES[0]}"
echo -e "${GREEN}✅ Found JAR file: $JAR_FILE${NC}"

# Verify SpringDoc dependencies for production builds
if [[ "$PROFILE" == "prod" ]]; then
  echo -e "${YELLOW}🔍 Verifying production build excludes SpringDoc dependencies...${NC}"

  if jar tf "$JAR_FILE" | grep -q "springdoc"; then
    echo -e "${RED}❌ ERROR: SpringDoc dependencies found in production build: $JAR_FILE${NC}"
    echo "   This indicates the production profile is not working correctly."
    exit 1
  fi

  echo -e "${GREEN}✅ Production build verification passed: No SpringDoc dependencies found${NC}"
fi

# ---------- DOCKER BUILD AND PUSH ----------

echo ""
echo -e "${YELLOW}🐳 Building and pushing Docker image...${NC}"

# Determine image tag based on profile
if [[ "$PROFILE" == "prod" ]]; then
  IMAGE_TAG="$REGISTRY_REPO:${SERVICE_NAME}-${VERSION}-prod"
else
  IMAGE_TAG="$REGISTRY_REPO:${SERVICE_NAME}-${VERSION}"
fi

CONTEXT_DIR="./${SERVICE_NAME}"
DOCKER_LOG="$BUILD_LOG_DIR/docker-build.log"

echo -e "${CYAN}——— Building $SERVICE_NAME  →  $IMAGE_TAG ———${NC}"

# Enable BuildKit features for better performance
export DOCKER_BUILDKIT=1

# Build and push the Docker image
{
  echo "🔨 Building Docker image for $SERVICE_NAME..."
  echo "Context: $CONTEXT_DIR"
  echo "Tag: $IMAGE_TAG"
  echo "Platform: $PLATFORM"
  echo ""

  if [[ "$BUILD_MODE" == "parallel" ]]; then
    echo "⚡ Using parallel optimizations..."
    # Use BuildKit parallel features
    docker buildx build \
      --platform "$PLATFORM" \
      -t "$IMAGE_TAG" \
      "$CONTEXT_DIR" \
      --push \
      --progress=plain \
      --cache-from=type=registry,ref="$IMAGE_TAG" \
      --cache-to=type=inline
  else
    echo "🔄 Using sequential build..."
    docker buildx build \
      --platform "$PLATFORM" \
      -t "$IMAGE_TAG" \
      "$CONTEXT_DIR" \
      --push \
      --progress=plain
  fi
} > "$DOCKER_LOG" 2>&1

# Check build result
if [[ $? -eq 0 ]]; then
  echo -e "${GREEN}✅ Docker image built and pushed successfully${NC}"
  BUILD_SUCCESS=true
else
  echo -e "${RED}❌ Docker build failed${NC}"
  echo ""
  echo -e "${YELLOW}📋 Last 20 lines of Docker build log:${NC}"
  tail -n 20 "$DOCKER_LOG" | sed 's/^/   /'
  echo ""
  echo -e "${CYAN}💡 Full Docker log available at: $DOCKER_LOG${NC}"
  exit 1
fi

# ---------- BUILD SUMMARY ----------

echo ""
echo -e "${GREEN}🎉 Single Service Build Completed Successfully!${NC}"
echo "=============================================="
echo ""
echo -e "${BLUE}📋 Build Summary:${NC}"
echo -e "   Service: ${CYAN}$SERVICE_NAME${NC}"
echo -e "   Version: ${CYAN}$VERSION${NC}"
echo -e "   Profile: ${CYAN}$PROFILE${NC}"
echo -e "   Build Mode: ${CYAN}$BUILD_MODE${NC}"
echo -e "   Platform: ${CYAN}$PLATFORM${NC}"
echo -e "   Image Tag: ${CYAN}$IMAGE_TAG${NC}"
echo -e "   JAR File: ${CYAN}$JAR_FILE${NC}"

if [[ "$PROFILE" == "prod" ]]; then
  echo -e "   ${YELLOW}🔒 Production image (no OpenAPI dependencies)${NC}"
else
  echo -e "   ${YELLOW}🛠️  Development image (with OpenAPI dependencies)${NC}"
fi

echo ""
echo -e "${BLUE}🚀 Next Steps:${NC}"
echo -e "   • Image is available in registry: ${CYAN}$IMAGE_TAG${NC}"
if [[ "$PROFILE" == "prod" ]]; then
  echo -e "   • Use with production docker-compose: ${CYAN}docker-compose.prod.yml${NC}"
  echo -e "   • Deploy with: ${CYAN}./deploy-prod.sh $VERSION${NC}"
else
  echo -e "   • Use with development docker-compose: ${CYAN}docker-compose.yml${NC}"
  echo -e "   • Start environment: ${CYAN}./start-docker.sh${NC}"
fi

echo ""
echo -e "${BLUE}🎯 Performance Benefits:${NC}"
echo -e "   • Single service build: ${GREEN}~2-5 minutes${NC} (vs ~15-20 min for all services)"
echo -e "   • Targeted Maven build: Only builds ${CYAN}$SERVICE_NAME${NC} and dependencies"
echo -e "   • Optimized Docker build: Uses BuildKit and caching"

echo ""
echo -e "${CYAN}💡 Tips for Development:${NC}"
echo -e "   • Use this script for rapid iteration on individual services"
echo -e "   • Combine with ${CYAN}./start-docker.sh --skip-build${NC} for faster testing"
echo -e "   • Use ${CYAN}--sequential${NC} flag for debugging build issues"

echo ""
echo -e "${GREEN}✅ Build logs available in: $BUILD_LOG_DIR${NC}"
