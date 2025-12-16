# Repository Setup Guide

This document provides comprehensive instructions for setting up the Hafnium development environment.

---

## Prerequisites

### Required Software

| Software | Minimum Version | Purpose |
|----------|----------------|---------|
| Docker | 24.0+ | Container runtime |
| Docker Compose | 2.20+ | Multi-container orchestration |
| Git | 2.40+ | Version control |
| Make | 4.0+ | Build automation |
| Node.js | 20.0+ | Frontend development |
| Python | 3.11+ | Risk engine and AI platform |
| Java | 21+ | Backend service |

### Optional Software

| Software | Version | Purpose |
|----------|---------|---------|
| kubectl | 1.28+ | Kubernetes management |
| Helm | 3.12+ | Kubernetes package management |
| Terraform | 1.5+ | Infrastructure provisioning |
| pre-commit | 3.5+ | Git hooks management |

---

## Initial Setup

### Step 1: Clone the Repository

```bash
git clone git@github.com:olaflaitinen/hafnium.git
cd hafnium
```

### Step 2: Configure Environment Variables

```bash
cp .env.example .env
```

Edit `.env` and configure the following required variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_PASSWORD` | PostgreSQL password | `hafnium_dev` |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin password | `admin` |
| `VAULT_DEV_ROOT_TOKEN_ID` | Vault development token | `dev-root-token` |

### Step 3: Install Pre-commit Hooks

```bash
pip install pre-commit
pre-commit install
```

### Step 4: Start the Development Stack

```bash
make up
```

This command starts all services defined in `docker-compose.yaml`.

### Step 5: Verify Installation

```bash
make health
```

---

## Service Endpoints

After startup, the following services are available:

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | N/A |
| Backend API | http://localhost:8080/api | JWT required |
| Risk Engine | http://localhost:8000 | JWT required |
| Keycloak | http://localhost:8081 | admin/admin |
| Grafana | http://localhost:3001 | admin/admin |
| Prometheus | http://localhost:9090 | N/A |
| Redpanda Console | http://localhost:8085 | N/A |

---

## Development Workflows

### Running Tests

```bash
# Run all tests
make test

# Run specific service tests
make test-risk-engine
make test-backend
make test-frontend
```

### Linting and Formatting

```bash
# Lint all services
make lint

# Format code
make format
```

### Database Operations

```bash
# Seed development data
make seed

# Reset database
make db-reset

# Run migrations
make db-migrate
```

---

## Troubleshooting

### Common Issues

#### Docker Compose Fails to Start

1. Ensure Docker daemon is running
2. Check for port conflicts: `docker ps`
3. Review logs: `make logs`

#### Database Connection Errors

1. Verify PostgreSQL is healthy: `docker compose ps postgres`
2. Check connection string in `.env`
3. Reset database: `make db-reset`

#### Authentication Failures

1. Verify Keycloak is running: `docker compose ps keycloak`
2. Check realm configuration
3. Obtain new token: `make token`

---

## Security Considerations

### Development Environment

The development environment uses relaxed security settings for convenience. **Do not use development configurations in production.**

Specific development-only settings:

- Vault runs in development mode
- TLS is disabled for local services
- Default passwords are used

### Secrets Management

Never commit secrets to the repository. Use the following practices:

1. Store secrets in `.env` (gitignored)
2. Use Vault for sensitive configuration
3. Rotate credentials regularly

---

## Next Steps

After completing setup:

1. Review [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines
2. Explore [docs/architecture/overview.md](docs/architecture/overview.md)
3. Read relevant ADRs in [docs/adrs/](docs/adrs/)

---

**DISCLAIMER**: This document provides technical setup instructions only. Compliance with organizational security policies and regulatory requirements is the responsibility of the implementing organization.

*Last Updated: 2025-12-16*
