#!/bin/bash

# Test script for configuration refresh functionality
# This script tests the Spring Cloud Config Server and refresh capabilities

echo "🔧 Testing Spring Cloud Config Server and Refresh Capabilities"
echo "=============================================================="

# Configuration
CONFIG_SERVER_URL="http://localhost:8888"
API_GATEWAY_URL="http://localhost:8080"
EUREKA_URL="http://localhost:8761"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "SUCCESS") echo -e "${GREEN}✅ $message${NC}" ;;
        "ERROR") echo -e "${RED}❌ $message${NC}" ;;
        "WARNING") echo -e "${YELLOW}⚠️  $message${NC}" ;;
        "INFO") echo -e "${BLUE}ℹ️  $message${NC}" ;;
    esac
}

# Function to test HTTP endpoint
test_endpoint() {
    local url=$1
    local description=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $description... "
    
    response=$(curl -s -w "%{http_code}" -o /tmp/response.txt "$url" 2>/dev/null)
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        print_status "SUCCESS" "$description (HTTP $http_code)"
        return 0
    else
        print_status "ERROR" "$description (HTTP $http_code, expected $expected_status)"
        return 1
    fi
}

# Function to test config server endpoints
test_config_server() {
    echo -e "\n${BLUE}📋 Testing Config Server Endpoints${NC}"
    echo "-----------------------------------"
    
    # Test config server health
    test_endpoint "$CONFIG_SERVER_URL/actuator/health" "Config Server Health"
    
    # Test configuration retrieval for different services and profiles
    test_endpoint "$CONFIG_SERVER_URL/api-gateway/default" "API Gateway Default Config"
    test_endpoint "$CONFIG_SERVER_URL/api-gateway/dev" "API Gateway Dev Config"
    test_endpoint "$CONFIG_SERVER_URL/api-gateway/docker" "API Gateway Docker Config"
    test_endpoint "$CONFIG_SERVER_URL/genai-service/default" "GenAI Service Default Config"
    test_endpoint "$CONFIG_SERVER_URL/system-admin-service/default" "System Admin Service Default Config"
    test_endpoint "$CONFIG_SERVER_URL/application/default" "Global Application Config"
    
    # Test config server info
    test_endpoint "$CONFIG_SERVER_URL/actuator/info" "Config Server Info"
}

# Function to test service discovery
test_service_discovery() {
    echo -e "\n${BLUE}🔍 Testing Service Discovery${NC}"
    echo "-----------------------------"
    
    # Test Eureka health
    test_endpoint "$EUREKA_URL/actuator/health" "Eureka Server Health"
    
    # Test registered services
    echo -n "Checking registered services... "
    services=$(curl -s "$EUREKA_URL/eureka/apps" 2>/dev/null | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort | uniq)
    
    if [ -n "$services" ]; then
        print_status "SUCCESS" "Found registered services:"
        echo "$services" | while read -r service; do
            echo "  - $service"
        done
    else
        print_status "WARNING" "No services found in Eureka registry"
    fi
}

# Function to test refresh endpoints
test_refresh_endpoints() {
    echo -e "\n${BLUE}🔄 Testing Refresh Endpoints${NC}"
    echo "-----------------------------"
    
    # List of services to test refresh endpoints
    local services=(
        "api-gateway:8080"
        "genai-service:8084"
        "system-admin-service:8086"
        "auth-service:8081"
        "clinic-admin-service:8083"
    )
    
    for service_port in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        echo -n "Testing $service refresh endpoint... "
        
        # Test if actuator endpoints are accessible
        response=$(curl -s -w "%{http_code}" -o /tmp/response.txt "http://localhost:$port/actuator/health" 2>/dev/null)
        http_code="${response: -3}"
        
        if [ "$http_code" = "200" ]; then
            # Test refresh endpoint
            refresh_response=$(curl -s -w "%{http_code}" -X POST -o /tmp/refresh_response.txt "http://localhost:$port/actuator/refresh" 2>/dev/null)
            refresh_code="${refresh_response: -3}"
            
            if [ "$refresh_code" = "200" ]; then
                print_status "SUCCESS" "$service refresh endpoint working"
            else
                print_status "WARNING" "$service refresh endpoint returned HTTP $refresh_code"
            fi
        else
            print_status "WARNING" "$service not accessible (HTTP $http_code)"
        fi
    done
}

# Function to test configuration properties
test_config_properties() {
    echo -e "\n${BLUE}⚙️  Testing Configuration Properties${NC}"
    echo "------------------------------------"
    
    # Test API Gateway configuration properties
    echo -n "Testing API Gateway config properties... "
    response=$(curl -s "http://localhost:8080/actuator/configprops" 2>/dev/null)
    
    if echo "$response" | grep -q "configurationProperties"; then
        print_status "SUCCESS" "API Gateway configuration properties accessible"
    else
        print_status "WARNING" "API Gateway configuration properties not accessible"
    fi
    
    # Test environment variables
    echo -n "Testing API Gateway environment... "
    env_response=$(curl -s "http://localhost:8080/actuator/env" 2>/dev/null)
    
    if echo "$env_response" | grep -q "propertySources"; then
        print_status "SUCCESS" "API Gateway environment accessible"
    else
        print_status "WARNING" "API Gateway environment not accessible"
    fi
}

# Function to test rate limiting configuration
test_rate_limiting_config() {
    echo -e "\n${BLUE}🚦 Testing Rate Limiting Configuration${NC}"
    echo "--------------------------------------"
    
    # Test system admin service rate limit endpoints
    test_endpoint "http://localhost:8086/api/system-admin/config/rate-limits" "Active Rate Limit Configurations"
    
    # Test API Gateway rate limit management
    test_endpoint "http://localhost:8080/management/rate-limit/health" "Rate Limit Management Health"
    test_endpoint "http://localhost:8080/management/rate-limit/stats" "Rate Limit Statistics"
}

# Main execution
main() {
    echo "Starting configuration and refresh testing..."
    echo "Please ensure all services are running before proceeding."
    echo
    
    # Wait for user confirmation
    read -p "Press Enter to continue or Ctrl+C to cancel..."
    
    # Run tests
    test_config_server
    test_service_discovery
    test_refresh_endpoints
    test_config_properties
    test_rate_limiting_config
    
    echo -e "\n${BLUE}📊 Test Summary${NC}"
    echo "==============="
    print_status "INFO" "Configuration testing completed"
    print_status "INFO" "Check the output above for any warnings or errors"
    
    echo -e "\n${YELLOW}💡 Next Steps:${NC}"
    echo "1. Create external Git repository for configuration"
    echo "2. Set CONFIG_GIT_URI environment variable"
    echo "3. Update configuration in Git repository"
    echo "4. Test refresh: curl -X POST http://localhost:8080/actuator/refresh"
    echo "5. Verify changes: curl http://localhost:8080/actuator/configprops"
}

# Cleanup function
cleanup() {
    rm -f /tmp/response.txt /tmp/refresh_response.txt
}

# Set trap for cleanup
trap cleanup EXIT

# Run main function
main "$@"
