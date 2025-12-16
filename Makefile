.PHONY: help up down restart logs clean test lint build seed

# Default target
help:
	@echo "Hafnium Development Commands"
	@echo ""
	@echo "Stack Management:"
	@echo "  make up           - Start all services"
	@echo "  make down         - Stop all services"
	@echo "  make restart      - Restart all services"
	@echo "  make logs         - Tail logs from all services"
	@echo "  make clean        - Remove containers, volumes, and images"
	@echo ""
	@echo "Development:"
	@echo "  make build        - Build all service images"
	@echo "  make test         - Run all tests"
	@echo "  make lint         - Run linters on all code"
	@echo "  make typecheck    - Run type checkers"
	@echo "  make format       - Format all code"
	@echo ""
	@echo "Data:"
	@echo "  make seed         - Seed database with demo data"
	@echo "  make reset-db     - Reset database to clean state"
	@echo ""
	@echo "AI Platform:"
	@echo "  make train        - Run model training pipeline"
	@echo "  make evaluate     - Evaluate model performance"
	@echo ""
	@echo "Contracts:"
	@echo "  make generate-sdk - Generate client SDKs from contracts"
	@echo "  make validate-contracts - Validate API contracts"

# =============================================================================
# STACK MANAGEMENT
# =============================================================================

up:
	docker compose up -d
	@echo "Waiting for services to be healthy..."
	@sleep 10
	@echo ""
	@echo "Services available at:"
	@echo "  Frontend:        http://localhost:3000"
	@echo "  Backend API:     http://localhost:8080"
	@echo "  Risk Engine:     http://localhost:8000"
	@echo "  Keycloak:        http://localhost:8081"
	@echo "  Grafana:         http://localhost:3001"
	@echo "  Redpanda Console: http://localhost:8083"
	@echo "  MinIO Console:   http://localhost:9001"

down:
	docker compose down

restart: down up

logs:
	docker compose logs -f

logs-%:
	docker compose logs -f $*

clean:
	docker compose down -v --rmi local
	docker system prune -f

# =============================================================================
# BUILD
# =============================================================================

build:
	docker compose build

build-%:
	docker compose build $*

# =============================================================================
# TESTING
# =============================================================================

test: test-python test-java test-frontend

test-python:
	cd services/risk-engine && pytest -v
	cd services/ai-platform && pytest -v

test-java:
	cd services/backend-java && ./gradlew test

test-frontend:
	cd services/frontend-react && npm test -- --watchAll=false

test-integration:
	docker compose -f docker-compose.yaml -f docker-compose.test.yaml up --abort-on-container-exit --exit-code-from integration-tests

test-e2e:
	cd tests/e2e && npm test

# =============================================================================
# LINTING AND FORMATTING
# =============================================================================

lint: lint-python lint-java lint-frontend

lint-python:
	cd services/risk-engine && ruff check .
	cd services/ai-platform && ruff check .

lint-java:
	cd services/backend-java && ./gradlew spotlessCheck

lint-frontend:
	cd services/frontend-react && npm run lint

typecheck: typecheck-python typecheck-frontend

typecheck-python:
	cd services/risk-engine && pyright
	cd services/ai-platform && pyright

typecheck-frontend:
	cd services/frontend-react && npm run typecheck

format: format-python format-java format-frontend

format-python:
	cd services/risk-engine && ruff format . && ruff check --fix .
	cd services/ai-platform && ruff format . && ruff check --fix .

format-java:
	cd services/backend-java && ./gradlew spotlessApply

format-frontend:
	cd services/frontend-react && npm run format

# =============================================================================
# DATABASE
# =============================================================================

seed:
	docker compose exec postgres psql -U hafnium -d hafnium -f /docker-entrypoint-initdb.d/seed.sql

reset-db:
	docker compose exec postgres psql -U hafnium -d hafnium -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	docker compose exec postgres psql -U hafnium -d hafnium -f /docker-entrypoint-initdb.d/001_schema.sql

migrate:
	cd services/backend-java && ./gradlew flywayMigrate

# =============================================================================
# AI PLATFORM
# =============================================================================

train:
	docker compose exec ai-inference python -m hafnium.ai.train

evaluate:
	docker compose exec ai-inference python -m hafnium.ai.evaluate

# =============================================================================
# CONTRACTS
# =============================================================================

generate-sdk:
	./scripts/generate-sdk.sh

validate-contracts:
	./scripts/validate-contracts.sh

# =============================================================================
# INFRASTRUCTURE
# =============================================================================

infra-plan:
	cd infra/terraform && terraform plan

infra-apply:
	cd infra/terraform && terraform apply

# =============================================================================
# UTILITIES
# =============================================================================

install-hooks:
	pre-commit install

shell-%:
	docker compose exec $* sh

psql:
	docker compose exec postgres psql -U hafnium -d hafnium

redis-cli:
	docker compose exec redis redis-cli
