#!/bin/bash

# Deploy script: pull latest stable images from Docker Hub and restart containers except PostgreSQL.
# Usage: ./deploy.sh [version]
# If no version provided, "latest" is used.

set -euo pipefail

# ---------- CONFIG ----------
REGISTRY_REPO="zm377/dentistdss-microservices"
SERVICES=(config-server discovery-server api-gateway auth-service oauth-service)
VERSION="${1:-latest}"
# The compose project name used both for tagging convention and compose commands
PROJECT_NAME="dentistdss"
# ---------------------------------

# Ensure we are in the directory that contains docker-compose.yml (repo root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f docker-compose.yml ]; then
  echo "❌ docker-compose.yml not found in $SCRIPT_DIR. Please run deploy.sh from the repository root or adjust SCRIPT_DIR."
  exit 1
fi

# Pull newest images and tag them for the local compose project
for svc in "${SERVICES[@]}"; do
  REMOTE_TAG="$REGISTRY_REPO:${svc}-${VERSION}"
  LOCAL_TAG="${PROJECT_NAME}_${svc}:latest"  # matches docker-compose default (<project>_<service>)

  echo "\n——— Pulling $REMOTE_TAG ———"
  docker pull "$REMOTE_TAG"

  echo "Retagging $REMOTE_TAG → $LOCAL_TAG (for docker-compose)"
  docker tag "$REMOTE_TAG" "$LOCAL_TAG" || true
done

echo "\nStopping existing containers (except postgres)…"
docker compose --project-name "$PROJECT_NAME" stop "${SERVICES[@]}" || true

echo "Removing old containers…"
docker compose --project-name "$PROJECT_NAME" rm -f "${SERVICES[@]}" || true

echo "Starting updated containers…"
docker compose --project-name "$PROJECT_NAME" up -d --no-build "${SERVICES[@]}"

echo "\n✅  Deployment complete. Containers now run the latest images." 