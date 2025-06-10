#!/bin/bash

# Deploy script: pull latest stable images from Docker Hub and restart containers except PostgreSQL and MongoDB.
# Usage: ./deploy.sh [version] [environment]
# If no version provided, "latest" is used.
# If no environment provided, "development" is used.
# Use "production" for production deployments with OpenAPI disabled.

set -euo pipefail

# ---------- CONFIG ----------
REGISTRY_REPO="zm377/dentistdss-microservices"
SERVICES=(config-server discovery-server api-gateway auth-service audit-service system-service genai-service clinic-service patient-service admin-server notification-service)
VERSION="${1:-latest}"
ENVIRONMENT="${2:-development}"
# The compose project name used both for tagging convention and compose commands
PROJECT_NAME="dentistdss"

# Validate environment
if [[ "$ENVIRONMENT" != "development" && "$ENVIRONMENT" != "production" ]]; then
  echo "❌ Invalid environment: $ENVIRONMENT. Use 'development' or 'production'"
  exit 1
fi

# Set compose file and image suffix based on environment
if [[ "$ENVIRONMENT" == "production" ]]; then
  COMPOSE_FILE="docker-compose.prod.yml"
  IMAGE_SUFFIX="-prod"
  echo "🔒 Production deployment: Using $COMPOSE_FILE with OpenAPI disabled"
else
  COMPOSE_FILE="docker-compose.yml"
  IMAGE_SUFFIX=""
  echo "🛠️  Development deployment: Using $COMPOSE_FILE with OpenAPI enabled"
fi
# ---------------------------------

# Ensure we are in the directory that contains docker-compose files (repo root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "❌ $COMPOSE_FILE not found in $SCRIPT_DIR. Please run deploy.sh from the repository root or adjust SCRIPT_DIR."
  exit 1
fi

# Pull newest images and tag them for the local compose project
for svc in "${SERVICES[@]}"; do
  REMOTE_TAG="$REGISTRY_REPO:${svc}-${VERSION}${IMAGE_SUFFIX}"
  LOCAL_TAG="${PROJECT_NAME}_${svc}:latest"  # matches docker-compose default (<project>_<service>)

  echo "\n——— Pulling $REMOTE_TAG ———"
  docker pull "$REMOTE_TAG"

  echo "Retagging $REMOTE_TAG → $LOCAL_TAG (for docker-compose)"
  docker tag "$REMOTE_TAG" "$LOCAL_TAG" || true
done

echo "\nStopping existing containers (except postgres and mongo)…"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" stop "${SERVICES[@]}" || true

echo "Removing old containers…"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" rm -f "${SERVICES[@]}" || true

echo "Starting updated containers…"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" up -d --no-build "${SERVICES[@]}"

# Verification for production deployments
if [[ "$ENVIRONMENT" == "production" ]]; then
  echo "\n🔍 Verifying production deployment..."
  sleep 5  # Wait for services to start

  # Test that OpenAPI endpoints are not accessible
  echo "Testing OpenAPI endpoint accessibility..."
  GATEWAY_URL="https://localhost:443"

  # Test a few key OpenAPI endpoints - they should return 404 or 403
  OPENAPI_ENDPOINTS=(
    "/v3/api-docs"
    "/swagger-ui.html"
    "/v3/api-docs/auth-service"
    "/swagger-ui/auth-service/"
  )

  for endpoint in "${OPENAPI_ENDPOINTS[@]}"; do
    HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL$endpoint" || echo "000")
    if [[ "$HTTP_CODE" == "200" ]]; then
      echo "❌ ERROR: OpenAPI endpoint $endpoint is accessible in production (HTTP $HTTP_CODE)"
      echo "   This is a security risk. OpenAPI should be disabled in production."
    elif [[ "$HTTP_CODE" == "404" || "$HTTP_CODE" == "403" ]]; then
      echo "✅ OpenAPI endpoint $endpoint properly blocked (HTTP $HTTP_CODE)"
    else
      echo "⚠️  OpenAPI endpoint $endpoint returned HTTP $HTTP_CODE (expected 404/403)"
    fi
  done
fi

echo "\n✅  Deployment complete. Containers now run the latest images."
echo "📋 Summary:"
echo "   Environment: $ENVIRONMENT"
echo "   Compose file: $COMPOSE_FILE"
echo "   Version: $VERSION"
if [[ "$ENVIRONMENT" == "production" ]]; then
  echo "   🔒 Production deployment with OpenAPI disabled"
  echo "   🔗 Access: https://localhost:443"
else
  echo "   🛠️  Development deployment with OpenAPI enabled"
  echo "   🔗 Access: https://localhost:443"
  echo "   📚 API Docs: https://localhost:443/swagger-ui.html"
fi