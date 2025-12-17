# Hafnium Backend Services

Production-grade Java microservices for the Hafnium AML/KYC compliance platform.

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         api-facade                               │
│                  (Unified Integration Surface)                   │
└─────────────────────────────┬────────────────────────────────────┘
                              │
    ┌─────────────────────────┼─────────────────────────┐
    │                         │                         │
    ▼                         ▼                         ▼
┌──────────┐            ┌──────────┐            ┌──────────┐
│ identity │            │screening │            │monitoring│
│ service  │            │ service  │            │ service  │
└────┬─────┘            └────┬─────┘            └────┬─────┘
     │                       │                       │
     │    ┌──────────────────┼───────────────────┐   │
     │    │                  │                   │   │
     ▼    ▼                  ▼                   ▼   ▼
┌──────────┐            ┌──────────┐            ┌──────────┐
│   case   │            │  vault   │            │   risk   │
│ service  │            │ service  │            │  engine  │
└──────────┘            └──────────┘            └──────────┘
                                                     │
                        ┌──────────┐                 │
                        │ signals  │◄────────────────┘
                        │ service  │
                        └──────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| `identity-service` | 8081 | Customer management, KYC orchestration |
| `screening-service` | 8082 | Sanctions/PEP fuzzy matching |
| `monitoring-service` | 8083 | Transaction monitoring, rule engine |
| `case-service` | 8084 | Investigation workflow management |
| `vault-service` | 8085 | PII tokenization boundary |
| `risk-engine-service` | 8086 | Unified risk scoring (rules + ML) |
| `signals-service` | 8087 | Device/session risk signals |
| `api-facade` | 8080 | Service aggregation layer |

## Quick Start

### Prerequisites

- Java 21+ (Temurin recommended)
- Docker Desktop
- Gradle 8.5+

### Development

```bash
# Start dependencies
cd ../..
docker compose up -d postgres redis redpanda keycloak opa

# Build all services
./gradlew build

# Run a specific service
./gradlew :identity-service:bootRun
```

### Testing

```bash
# Unit tests
./gradlew test

# Integration tests with Testcontainers
./gradlew integrationTest

# All tests with coverage
./gradlew test jacocoTestReport
```

### Docker

```bash
# Build all images
./gradlew bootBuildImage

# Or individually
docker build -t hafnium/identity-service ./identity-service
```

## Project Structure

```
backend-java/
├── common/                    # Shared libraries
│   ├── common-model/          # Domain models, events, DTOs
│   ├── common-security/       # JWT, tenant context
│   ├── common-kafka/          # Event publishing
│   ├── common-web/            # Exception handling
│   └── common-authz/          # OPA client
├── identity-service/          # KYC orchestration
├── screening-service/         # Sanctions matching
├── monitoring-service/        # Transaction monitoring
├── case-service/              # Case management
├── vault-service/             # PII tokenization
├── risk-engine-service/       # Risk scoring
├── signals-service/           # Security signals
├── api-facade/                # API gateway
├── build.gradle               # Root build config
└── settings.gradle            # Module definitions
```

## Configuration

All services use Spring Boot configuration with these common properties:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_AUTH_SERVER_URL}/realms/hafnium
```

Environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/hafnium` | Database URL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `KEYCLOAK_AUTH_SERVER_URL` | `http://localhost:8081` | Keycloak URL |
| `OPA_URL` | `http://localhost:8181` | OPA endpoint |

## Security

- **Authentication**: JWT tokens via Keycloak
- **Authorization**: OPA policies for fine-grained access control
- **Multi-tenancy**: Tenant isolation via JWT claims and RLS
- **PII Protection**: Tokenization via vault-service

## Event-Driven Architecture

All services emit events to Kafka topics:

| Event | Topic | Services |
|-------|-------|----------|
| `customer.created` | `hf.customer.created.v1` | identity |
| `kyc.requested` | `hf.kyc.requested.v1` | identity |
| `screening.completed` | `hf.screening.completed.v1` | screening |
| `txn.ingested` | `hf.txn.ingested.v1` | monitoring |
| `alert.raised` | `hf.alert.raised.v1` | monitoring |
| `case.created` | `hf.case.created.v1` | case |
| `risk.scored` | `hf.risk.scored.v1` | risk-engine |

## Observability

- **Metrics**: Prometheus endpoint at `/actuator/prometheus`
- **Health**: Liveness/readiness probes at `/actuator/health`
- **Tracing**: OpenTelemetry via `X-Trace-ID` header
- **Logging**: Structured JSON logs with correlation IDs

## License

Copyright (c) 2024 Hafnium. All rights reserved.
