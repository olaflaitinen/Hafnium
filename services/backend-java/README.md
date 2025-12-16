# Hafnium Backend Services

## Overview

The Hafnium backend is implemented as a set of Java 21 microservices using Spring Boot 3.2.
These services provide the core compliance and risk management functionality for the platform.

## Service Catalog

| Service | Port | Description |
|---------|------|-------------|
| identity-service | 8080 | KYC workflow orchestration, customer onboarding |
| screening-service | 8081 | Sanctions and PEP screening with fuzzy matching |
| monitoring-service | 8082 | Transaction monitoring and alerting |
| case-service | 8083 | Investigation case management with state machine |
| vault-service | 8084 | PII tokenization boundary with encryption |
| risk-engine-service | 8085 | Unified risk scoring and decision API |
| signals-service | 8086 | Security signals and step-up policy |
| api-facade | 8087 | Stable integration surface for frontend |

## Architecture

```
+-------------------+     +-------------------+
|   API Gateway     |---->|    API Facade     |
| (Envoy/NGINX)     |     | (Routing/Auth)    |
+-------------------+     +-------------------+
                                  |
         +------------------------+------------------------+
         |            |           |           |            |
         v            v           v           v            v
+----------------+ +----------------+ +----------------+ +----------------+
| Identity Svc   | | Screening Svc  | | Monitoring Svc | | Case Service   |
| (KYC)          | | (AML Lists)    | | (Transactions) | | (Workflow)     |
+----------------+ +----------------+ +----------------+ +----------------+
         |            |           |           |
         v            v           v           v
+----------------+ +----------------+ +----------------+
| Vault Service  | | Risk Engine    | | Signals Svc    |
| (Tokenization) | | (Scoring)      | | (Step-up)      |
+----------------+ +----------------+ +----------------+
         |            |           |           |
         +------------+-----------+-----------+
                      |
              +-------v-------+
              |   Postgres    |
              |   (Schemas)   |
              +---------------+
```

## Technology Stack

- **Runtime**: Java 21, Spring Boot 3.2
- **Build**: Gradle 8.5 multi-project
- **Database**: PostgreSQL 16 with Flyway migrations
- **Messaging**: Apache Kafka (Redpanda)
- **Cache**: Redis 7
- **Security**: Spring Security OAuth2, Keycloak, OPA
- **Observability**: Micrometer, OpenTelemetry, Prometheus

## Quick Start

### Prerequisites

- Java 21+
- Docker and Docker Compose
- Gradle 8.5+

### Build

```bash
cd services/backend-java
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Start Services

```bash
# From repository root
make up

# Or directly
docker compose up -d
```

### Code Quality

```bash
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run linting
./gradlew build
```

## Project Structure

```
services/backend-java/
├── build.gradle              # Root build configuration
├── settings.gradle           # Multi-project settings
├── gradle.properties         # Build properties
├── common/                   # Shared libraries
│   ├── common-model/         # Domain models, DTOs
│   ├── common-security/      # JWT, tenant context
│   ├── common-kafka/         # Event publishing
│   ├── common-web/           # REST utilities
│   └── common-authz/         # OPA authorization
├── identity-service/         # KYC orchestration
├── screening-service/        # Sanctions matching
├── monitoring-service/       # Transaction monitoring
├── case-service/             # Case management
├── vault-service/            # Tokenization
├── risk-engine-service/      # Risk scoring
├── signals-service/          # Security signals
└── api-facade/               # Frontend gateway
```

## Configuration

Services are configured via environment variables with sensible defaults for local development.

Key environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| DB_HOST | localhost | PostgreSQL host |
| DB_PORT | 5432 | PostgreSQL port |
| DB_NAME | hafnium | Database name |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| KEYCLOAK_ISSUER_URI | http://localhost:8081/realms/hafnium | Keycloak issuer |
| OPA_URL | http://localhost:8181 | OPA server URL |

## API Documentation

Each service exposes OpenAPI documentation at `/swagger-ui.html` when running.

Contracts are defined in `/contracts/openapi/`.

## Development Guidelines

1. **Code Style**: Google Java Format enforced via Spotless
2. **Error Handling**: Use RFC 7807 Problem Details
3. **Logging**: Structured JSON logs with trace IDs
4. **Testing**: JUnit 5 + Testcontainers for integration tests
5. **Commits**: Conventional Commits format

## License

Apache License 2.0
