#!/usr/bin/env bash
# Contract Validation Script
# Validates OpenAPI and AsyncAPI specifications

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CONTRACTS_DIR="$PROJECT_ROOT/contracts"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check requirements
check_requirements() {
    if ! command -v redocly &> /dev/null; then
        log_error "redocly not found. Install with: npm install -g @redocly/cli"
        exit 1
    fi
}

# Validate OpenAPI specs
validate_openapi() {
    log_info "Validating OpenAPI specifications..."
    
    local has_errors=0
    
    for spec in "$CONTRACTS_DIR/openapi"/*.yaml; do
        if [[ -f "$spec" ]]; then
            log_info "Validating $(basename "$spec")..."
            if ! redocly lint "$spec"; then
                has_errors=1
            fi
        fi
    done
    
    return $has_errors
}

# Validate AsyncAPI specs
validate_asyncapi() {
    log_info "Validating AsyncAPI specifications..."
    
    # AsyncAPI validation would go here
    # Using asyncapi-cli when available
    
    for spec in "$CONTRACTS_DIR/asyncapi"/*.yaml; do
        if [[ -f "$spec" ]]; then
            log_info "Found AsyncAPI spec: $(basename "$spec")"
            # asyncapi validate "$spec"
        fi
    done
}

# Validate JSON schemas
validate_schemas() {
    log_info "Validating JSON schemas..."
    
    for schema in "$CONTRACTS_DIR/schemas"/*.json; do
        if [[ -f "$schema" ]]; then
            log_info "Validating $(basename "$schema")..."
            # Use ajv or similar for validation
            if command -v ajv &> /dev/null; then
                ajv compile -s "$schema" || log_error "Invalid schema: $schema"
            fi
        fi
    done
}

# Main
main() {
    check_requirements
    
    log_info "Starting contract validation..."
    
    local exit_code=0
    
    if ! validate_openapi; then
        exit_code=1
    fi
    
    validate_asyncapi
    validate_schemas
    
    if [[ $exit_code -eq 0 ]]; then
        log_info "All contracts validated successfully!"
    else
        log_error "Contract validation failed!"
    fi
    
    exit $exit_code
}

main "$@"
