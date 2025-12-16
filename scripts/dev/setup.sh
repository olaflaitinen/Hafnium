#!/usr/bin/env bash
# Development environment setup script

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    local missing=()
    
    if ! command -v docker &> /dev/null; then
        missing+=("docker")
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        missing+=("docker-compose")
    fi
    
    if ! command -v make &> /dev/null; then
        missing+=("make")
    fi
    
    if [ ${#missing[@]} -ne 0 ]; then
        log_error "Missing prerequisites: ${missing[*]}"
        exit 1
    fi
    
    log_info "All prerequisites satisfied"
}

setup_env() {
    log_info "Setting up environment..."
    
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
        log_info "Created .env from template"
    else
        log_warn ".env already exists, skipping"
    fi
}

setup_pre_commit() {
    log_info "Setting up pre-commit hooks..."
    
    if command -v pre-commit &> /dev/null; then
        cd "$PROJECT_ROOT"
        pre-commit install
        log_info "Pre-commit hooks installed"
    else
        log_warn "pre-commit not installed, skipping"
        log_info "Install with: pip install pre-commit"
    fi
}

pull_images() {
    log_info "Pulling Docker images..."
    
    cd "$PROJECT_ROOT"
    docker compose pull
    log_info "Docker images pulled"
}

main() {
    log_info "Starting development environment setup..."
    
    check_prerequisites
    setup_env
    setup_pre_commit
    pull_images
    
    log_info "Setup complete!"
    log_info ""
    log_info "Next steps:"
    log_info "  1. Review and update .env if needed"
    log_info "  2. Run 'make up' to start the development stack"
    log_info "  3. Run 'make health' to verify services"
}

main "$@"
