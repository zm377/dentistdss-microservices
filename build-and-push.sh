#!/bin/bash

# Build and push micro-service images for linux/amd64
# Usage: ./build-and-push.sh [version]
# If no version provided, "latest" is used.

set -euo pipefail

# ---------- CONFIG ----------
REGISTRY_REPO="zm377/dentistdss-microservices"
SERVICES=(config-server discovery-server api-gateway auth-service oauth-service)
VERSION="${1:-latest}"
PLATFORM="linux/amd64"
BUILDX_BUILDER="dentist-multiarch"
# ----------------------------

# Ensure buildx builder exists
if ! docker buildx inspect "$BUILDX_BUILDER" >/dev/null 2>&1; then
  echo "Creating buildx builder $BUILDX_BUILDER (first-time setup) …"
  docker buildx create --name "$BUILDX_BUILDER" --use
fi

echo "Bootstrapping builder ($BUILDX_BUILDER)…"
docker buildx inspect --bootstrap "$BUILDX_BUILDER" >/dev/null

# Build all Maven modules first so that <module>/target/*.jar exist for the Docker COPY commands.
echo "Running Maven build (skip tests) …"
./mvnw -q clean package -DskipTests

echo "Building images for platform $PLATFORM and pushing to $REGISTRY_REPO …"

for svc in "${SERVICES[@]}"; do
  CONTEXT_DIR="./${svc}"
  IMAGE_TAG="$REGISTRY_REPO:${svc}-${VERSION}"

  echo "\n——— Building $svc  →  $IMAGE_TAG ———"
  docker buildx build \
    --platform "$PLATFORM" \
    -t "$IMAGE_TAG" \
    "$CONTEXT_DIR" \
    --push

done

echo "\n✅  All images built and pushed successfully." 