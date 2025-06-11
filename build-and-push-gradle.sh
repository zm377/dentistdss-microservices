#!/bin/bash

# Build and push microservices with Gradle and parallel support
# Usage: ./build-and-push-gradle.sh [version] [profile] [--parallel|--sequential] [--max-parallel=N]

set -euo pipefail

# Config
REGISTRY_REPO="zm377/dentistdss-microservices"
SERVICES=(config-server discovery-server api-gateway auth-service audit-service system-admin-service genai-service clinic-admin-service appointment-service clinical-records-service user-profile-service admin-server notification-service chat-log-service reporting-service)

VERSION="${1:-latest}"
PROFILE="${2:-docker}"
PLATFORM="linux/amd64"
BUILD_MODE="parallel"
MAX_PARALLEL=16

# Parse args
for arg in "${@:3}"; do
  case $arg in
    --parallel) BUILD_MODE="parallel" ;;
    --sequential) BUILD_MODE="sequential" ;;
    --max-parallel=*)
      MAX_PARALLEL="${arg#*=}"
      [[ "$MAX_PARALLEL" =~ ^[0-9]+$ ]] || { echo "❌ Invalid max-parallel: $MAX_PARALLEL"; exit 1; }
      ;;
    *) echo "❌ Unknown arg: $arg"; exit 1 ;;
  esac
done

# Validate profile
[[ "$PROFILE" =~ ^(dev|docker|prod)$ ]] || { echo "❌ Invalid profile: $PROFILE"; exit 1; }

echo "🔧 Profile: $PROFILE | Mode: $BUILD_MODE | Max parallel: $MAX_PARALLEL"
[[ "$PROFILE" == "prod" ]] && echo "🔒 Production build (no OpenAPI)" || echo "🛠️ Development build (with OpenAPI)"

# Simple tracking without associative arrays
BUILD_LOG_DIR="/tmp/dentist-build-$$"
FAILED_BUILDS=""

cleanup() {
  pkill -P $$ 2>/dev/null || true
  rm -rf "$BUILD_LOG_DIR" 2>/dev/null || true
}
trap cleanup EXIT INT TERM
mkdir -p "$BUILD_LOG_DIR"

build_service() {
  local svc="$1"
  local tag="$REGISTRY_REPO:${svc}-${VERSION}$([[ "$PROFILE" == "prod" ]] && echo "-prod")"
  local log="$BUILD_LOG_DIR/${svc}.log"

  echo "🔨 Building $svc → $tag"

  if DOCKER_BUILDKIT=1 docker buildx build --platform "$PLATFORM" -t "$tag" "./$svc" --push --progress=plain &>"$log"; then
    echo "SUCCESS" > "$BUILD_LOG_DIR/${svc}.status"
    echo "✅ $svc"
  else
    echo "FAILED" > "$BUILD_LOG_DIR/${svc}.status"
    echo "❌ $svc failed"
    tail -5 "$log" | sed 's/^/   /'
    return 1
  fi
}

wait_for_builds() {
  local services=("$@")
  echo "⏳ Waiting for: ${services[*]}"

  for svc in "${services[@]}"; do
    local pid_file="$BUILD_LOG_DIR/${svc}.pid"
    if [[ -f "$pid_file" ]]; then
      local pid=$(cat "$pid_file")
      wait "$pid" 2>/dev/null || true

      if [[ -f "$BUILD_LOG_DIR/${svc}.status" ]] && [[ "$(cat "$BUILD_LOG_DIR/${svc}.status")" == "SUCCESS" ]]; then
        echo "✅ $svc completed"
      else
        echo "❌ $svc failed"
        FAILED_BUILDS="$FAILED_BUILDS $svc"
        return 1
      fi
    fi
  done
}

# Setup buildx
BUILDX_BUILDER="dentist-multiarch"
if ! docker buildx inspect "$BUILDX_BUILDER" &>/dev/null; then
  docker buildx create --name "$BUILDX_BUILDER" --use
fi
docker buildx inspect --bootstrap "$BUILDX_BUILDER" &>/dev/null

# Gradle build
echo "🔨 Gradle build with profile: $PROFILE"
./gradlew clean bootJar -Pprofile="$PROFILE" --parallel --build-cache

# Verify production build
if [[ "$PROFILE" == "prod" ]]; then
  echo "🔍 Verifying no SpringDoc in production..."
  for svc in auth-service api-gateway genai-service; do
    jar_file=$(ls "./${svc}/build/libs/${svc}-"*.jar 2>/dev/null | head -n1)
    if [[ -f "$jar_file" ]] && jar tf "$jar_file" | grep -q "springdoc"; then
      echo "❌ SpringDoc found in $jar_file"; exit 1
    fi
  done
  echo "✅ Production verification passed"
fi

echo "🚀 Building images for $PLATFORM → $REGISTRY_REPO"

start_builds() {
  local services=("$@")
  for svc in "${services[@]}"; do
    build_service "$svc" &
    echo $! > "$BUILD_LOG_DIR/${svc}.pid"
    echo "🚀 Started $svc"
    sleep 0.1
  done
}

# Build execution
if [[ "$BUILD_MODE" == "parallel" ]]; then
  echo "📦 Tier 1: Foundation"
  start_builds config-server
  wait_for_builds config-server || exit 1

  echo "📦 Tier 2: Discovery"
  start_builds discovery-server
  wait_for_builds discovery-server || exit 1

  echo "📦 Tier 3: Gateway"
  start_builds api-gateway
  wait_for_builds api-gateway || exit 1

  echo "📦 Tier 4: Services"
  start_builds auth-service audit-service system-admin-service genai-service clinic-admin-service appointment-service clinical-records-service user-profile-service admin-server notification-service chat-log-service reporting-service
  wait_for_builds auth-service audit-service system-admin-service genai-service clinic-admin-service appointment-service clinical-records-service user-profile-service admin-server notification-service chat-log-service reporting-service || exit 1
else
  echo "🔄 Sequential builds..."
  for svc in "${SERVICES[@]}"; do
    build_service "$svc" || { FAILED_BUILDS="$FAILED_BUILDS $svc"; exit 1; }
  done
fi

# Final status
if [[ -z "$FAILED_BUILDS" ]]; then
  echo ""
  echo "✅ All images built successfully!"
  echo "📋 Profile: $PROFILE | Version: $VERSION | Mode: $BUILD_MODE"
  [[ "$PROFILE" == "prod" ]] && echo "🔒 Production images (no OpenAPI)" || echo "🛠️ Development images (with OpenAPI)"
else
  echo ""
  echo "❌ Build failed! Services:$FAILED_BUILDS"
  echo "📁 Logs: $BUILD_LOG_DIR"
  exit 1
fi
