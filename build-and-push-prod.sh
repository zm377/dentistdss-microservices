#!/bin/bash

# Production build script - builds with prod profile (no OpenAPI)
# Usage: ./build-and-push-prod.sh [version] [build-options...]

set -euo pipefail

VERSION="${1:-latest}"
BUILD_ARGS=("${@:2}")

echo "🔒 PRODUCTION BUILD (OpenAPI disabled)"
echo "   Version: $VERSION"
[[ ${#BUILD_ARGS[@]} -gt 0 ]] 2>/dev/null && echo "   Options: ${BUILD_ARGS[*]}"

./build-and-push.sh "$VERSION" "prod" "${BUILD_ARGS[@]+"${BUILD_ARGS[@]}"}"

echo ""
echo "✅ Production build completed!"
echo "📋 Deploy: ./deploy-prod.sh $VERSION"
echo "🔒 OpenAPI is disabled for security"
