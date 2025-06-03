#!/bin/bash

# Production deployment script - deploys with production configuration (no OpenAPI)
# Usage: ./deploy-prod.sh [version]
# If no version provided, "latest" is used.

set -euo pipefail

VERSION="${1:-latest}"

echo "🔒 Deploying PRODUCTION environment (OpenAPI disabled)"
echo "   Version: $VERSION"
echo "   Environment: production"
echo "   Compose file: docker-compose.prod.yml"
echo ""

# Call the main deploy script with production environment
./deploy.sh "$VERSION" "production"

echo ""
echo "✅ Production deployment completed!"
