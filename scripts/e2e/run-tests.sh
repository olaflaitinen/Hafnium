#!/usr/bin/env bash
# End-to-end test runner

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
RISK_ENGINE_URL="${RISK_ENGINE_URL:-http://localhost:8000}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_test() {
    echo -e "${YELLOW}[TEST]${NC} $1"
}

# Get authentication token
get_token() {
    log_info "Obtaining authentication token..."
    
    TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/hafnium/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=hafnium-api" \
        -d "client_secret=change-me-in-production" \
        -d "username=analyst" \
        -d "password=analyst" | jq -r '.access_token')
    
    if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
        log_error "Failed to obtain token"
        exit 1
    fi
    
    log_info "Token obtained successfully"
}

# Test health endpoints
test_health() {
    log_test "Testing health endpoints..."
    
    # Backend health
    if curl -sf "$API_BASE_URL/actuator/health" > /dev/null; then
        log_info "Backend health: OK"
    else
        log_error "Backend health: FAILED"
        return 1
    fi
    
    # Risk Engine health
    if curl -sf "$RISK_ENGINE_URL/health" > /dev/null; then
        log_info "Risk Engine health: OK"
    else
        log_error "Risk Engine health: FAILED"
        return 1
    fi
}

# Test customer API
test_customer_api() {
    log_test "Testing Customer API..."
    
    # Create customer
    RESPONSE=$(curl -s -X POST "$API_BASE_URL/api/v1/customers" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"externalId": "E2E-TEST-001", "customerType": "individual"}')
    
    CUSTOMER_ID=$(echo "$RESPONSE" | jq -r '.customerId')
    
    if [ "$CUSTOMER_ID" != "null" ] && [ -n "$CUSTOMER_ID" ]; then
        log_info "Create customer: OK (ID: $CUSTOMER_ID)"
    else
        log_error "Create customer: FAILED"
        echo "$RESPONSE"
        return 1
    fi
    
    # Get customer
    RESPONSE=$(curl -s -X GET "$API_BASE_URL/api/v1/customers/$CUSTOMER_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$RESPONSE" | jq -e '.customerId' > /dev/null; then
        log_info "Get customer: OK"
    else
        log_error "Get customer: FAILED"
        return 1
    fi
}

# Test risk scoring
test_risk_scoring() {
    log_test "Testing Risk Scoring API..."
    
    RESPONSE=$(curl -s -X POST "$RISK_ENGINE_URL/api/v1/risk/score" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "entity_type": "customer",
            "entity_id": "test-customer-001",
            "context": {
                "use_case": "e2e_test",
                "amount": 1000.00
            }
        }')
    
    SCORE=$(echo "$RESPONSE" | jq -r '.score')
    
    if [ "$SCORE" != "null" ] && [ -n "$SCORE" ]; then
        log_info "Risk scoring: OK (Score: $SCORE)"
    else
        log_error "Risk scoring: FAILED"
        echo "$RESPONSE"
        return 1
    fi
}

# Main
main() {
    log_info "Starting end-to-end tests..."
    
    local failed=0
    
    test_health || ((failed++))
    get_token || ((failed++))
    test_customer_api || ((failed++))
    test_risk_scoring || ((failed++))
    
    echo ""
    if [ $failed -eq 0 ]; then
        log_info "All tests passed!"
        exit 0
    else
        log_error "$failed test(s) failed"
        exit 1
    fi
}

main "$@"
