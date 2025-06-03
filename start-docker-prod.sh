#!/bin/bash

# Start Docker production environment (no OpenAPI) with parallel building support
# Usage: ./start-docker-prod.sh [--parallel|--sequential] [--max-parallel=N] [--skip-build]
# Supports all optimization options from the main start-docker script.

BUILD_ARGS=("$@")  # Pass through all arguments

echo "🔒 Starting Docker PRODUCTION environment (OpenAPI disabled)"
echo "   Profile: prod"
echo "   Compose file: docker-compose.prod.yml"
if [[ ${#BUILD_ARGS[@]} -gt 0 ]]; then
  echo "   Build Options: ${BUILD_ARGS[*]}"
fi
echo ""

# Call the main start-docker script with production profile and pass through all arguments
./start-docker.sh "prod" "${BUILD_ARGS[@]}"
