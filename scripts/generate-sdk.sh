#!/usr/bin/env bash
# SDK Generation Script
# Generates client SDKs from OpenAPI specifications

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CONTRACTS_DIR="$PROJECT_ROOT/contracts/openapi"
OUTPUT_DIR="$PROJECT_ROOT/libs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check for required tools
check_requirements() {
    if ! command -v openapi-generator-cli &> /dev/null; then
        log_error "openapi-generator-cli not found. Install with: npm install -g @openapitools/openapi-generator-cli"
        exit 1
    fi
}

# Generate Python client
generate_python() {
    local spec=$1
    local name=$2
    local output="$OUTPUT_DIR/python/hafnium-${name}-client"
    
    log_info "Generating Python client for $name..."
    
    openapi-generator-cli generate \
        -i "$spec" \
        -g python \
        -o "$output" \
        --additional-properties=packageName=hafnium_${name}_client \
        --additional-properties=packageVersion=1.0.0 \
        --additional-properties=projectName=hafnium-${name}-client
    
    log_info "Python client generated at $output"
}

# Generate Java client
generate_java() {
    local spec=$1
    local name=$2
    local output="$OUTPUT_DIR/java/hafnium-${name}-client"
    
    log_info "Generating Java client for $name..."
    
    openapi-generator-cli generate \
        -i "$spec" \
        -g java \
        -o "$output" \
        --additional-properties=groupId=dev.hafnium \
        --additional-properties=artifactId=hafnium-${name}-client \
        --additional-properties=artifactVersion=1.0.0 \
        --additional-properties=library=native \
        --additional-properties=useJakartaEe=true
    
    log_info "Java client generated at $output"
}

# Generate TypeScript client
generate_typescript() {
    local spec=$1
    local name=$2
    local output="$PROJECT_ROOT/services/frontend-react/src/api/${name}"
    
    log_info "Generating TypeScript client for $name..."
    
    openapi-generator-cli generate \
        -i "$spec" \
        -g typescript-axios \
        -o "$output" \
        --additional-properties=supportsES6=true \
        --additional-properties=withInterfaces=true
    
    log_info "TypeScript client generated at $output"
}

# Main
main() {
    check_requirements
    
    log_info "Starting SDK generation..."
    
    # Create output directories
    mkdir -p "$OUTPUT_DIR/python"
    mkdir -p "$OUTPUT_DIR/java"
    
    # Generate clients for Risk Engine
    if [[ -f "$CONTRACTS_DIR/risk-engine.yaml" ]]; then
        generate_python "$CONTRACTS_DIR/risk-engine.yaml" "risk"
        generate_java "$CONTRACTS_DIR/risk-engine.yaml" "risk"
        generate_typescript "$CONTRACTS_DIR/risk-engine.yaml" "risk"
    fi
    
    # Generate clients for Identity Service
    if [[ -f "$CONTRACTS_DIR/identity-service.yaml" ]]; then
        generate_python "$CONTRACTS_DIR/identity-service.yaml" "identity"
        generate_java "$CONTRACTS_DIR/identity-service.yaml" "identity"
        generate_typescript "$CONTRACTS_DIR/identity-service.yaml" "identity"
    fi
    
    log_info "SDK generation complete!"
}

main "$@"
