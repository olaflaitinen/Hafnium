# Backend Architecture

## Overview

The Hafnium backend is implemented as a microservices architecture using Spring Boot 3.2 and Java 21. Services communicate via REST APIs and asynchronous events through Kafka/Redpanda.

## Service Architecture

```mermaid
flowchart TB
    subgraph Edge["Edge Layer"]
        AG[API Gateway - Envoy]
        AF[API Facade]
    end

    subgraph Auth["Authentication/Authorization"]
        KC[Keycloak]
        OPA[Open Policy Agent]
    end

    subgraph Services["Application Services"]
        IS[Identity Service]
        SS[Screening Service]
        MS[Monitoring Service]
        CS[Case Service]
        VS[Vault Service]
        RS[Risk Engine Service]
        SG[Signals Service]
    end

    subgraph Data["Data Layer"]
        PG[(PostgreSQL)]
        RD[(Redis)]
        RP[Redpanda/Kafka]
        MO[(MinIO)]
    end

    subgraph ML["AI/ML Platform"]
        AI[AI Inference]
        MF[MLflow Registry]
    end

    AG --> AF
    AF --> IS & SS & MS & CS & VS & RS & SG

    IS & SS & MS & CS & VS & RS & SG --> KC
    IS & SS & MS & CS & VS & RS & SG --> OPA
    IS & SS & MS & CS & VS & RS & SG --> PG
    IS & SS & MS & CS & VS & RS & SG --> RD
    IS & SS & MS & CS & VS & RS & SG --> RP

    CS --> MO
    RS --> AI
    AI --> MF
```

## Event-Driven Architecture

```mermaid
flowchart LR
    subgraph Producers
        IS[Identity Service]
        MS[Monitoring Service]
        RS[Risk Engine]
        CS[Case Service]
        VS[Vault Service]
    end

    subgraph Kafka["Redpanda/Kafka Topics"]
        T1[hf.txn.ingested.v1]
        T2[hf.txn.scored.v1]
        T3[hf.alert.raised.v1]
        T4[hf.case.created.v1]
        T5[hf.kyc.completed.v1]
        T6[hf.risk.scored.v1]
    end

    subgraph Consumers
        SP[Stream Processor]
        AN[Analytics]
        AU[Audit Log]
    end

    MS --> T1
    RS --> T2 & T6
    MS --> T3
    CS --> T4
    IS --> T5

    T1 & T2 & T3 & T4 & T5 & T6 --> SP & AN & AU
```

## Data Model

```mermaid
erDiagram
    TENANT ||--o{ CUSTOMER : has
    CUSTOMER ||--o{ KYC_WORKFLOW : has
    CUSTOMER ||--o{ DOCUMENT : has
    CUSTOMER ||--o{ TRANSACTION : has
    TRANSACTION ||--o{ ALERT : triggers
    ALERT ||--o{ CASE : creates
    CASE ||--o{ EVIDENCE : contains
    CUSTOMER ||--o{ SCREENING_MATCH : has
    TRANSACTION ||--o{ RISK_DECISION : has

    TENANT {
        uuid id PK
        string name
        string status
        jsonb settings
    }

    CUSTOMER {
        uuid id PK
        uuid tenant_id FK
        string external_id
        string customer_type
        string status
        string risk_tier
    }

    TRANSACTION {
        uuid id PK
        uuid tenant_id FK
        uuid customer_id FK
        decimal amount
        string currency
        timestamp txn_timestamp
    }

    RISK_DECISION {
        uuid id PK
        uuid tenant_id FK
        string entity_type
        string entity_id
        decimal score
        string risk_level
        jsonb reasons
    }
```

## Service Catalog

| Service | Port | Schema | Description |
|---------|------|--------|-------------|
| identity-service | 8080 | identity | KYC workflow orchestration |
| screening-service | 8081 | screening | Sanctions/PEP matching |
| monitoring-service | 8082 | monitoring | Transaction monitoring |
| case-service | 8083 | cases | Case management |
| vault-service | 8084 | vault | PII tokenization |
| risk-engine-service | 8085 | risk | Unified risk scoring |
| signals-service | 8086 | signals | Security signals |
| api-facade | 8087 | - | API gateway |

## Security Architecture

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant KC as Keycloak
    participant S as Service
    participant OPA as OPA

    C->>KC: Authenticate
    KC-->>C: JWT Token

    C->>G: Request + JWT
    G->>S: Forward with JWT

    S->>S: Validate JWT
    S->>S: Extract tenant_id, roles

    S->>OPA: Authorization query
    OPA-->>S: Allow/Deny

    alt Authorized
        S-->>G: Response
        G-->>C: Response
    else Denied
        S-->>G: 403 Forbidden
        G-->>C: 403 Forbidden
    end
```

## Risk Scoring Flow

```mermaid
flowchart TB
    subgraph Input
        REQ[Score Request]
    end

    subgraph Rules["Rule Engine"]
        R1[Amount Rules]
        R2[Velocity Rules]
        R3[Geographic Rules]
    end

    subgraph ML["ML Models"]
        M1[Anomaly Detection]
        M2[Risk Classifier]
    end

    subgraph Aggregation
        AGG[Score Aggregator]
    end

    subgraph Output
        DEC[Decision]
        AUD[Audit Log]
        EVT[Event]
    end

    REQ --> R1 & R2 & R3
    REQ --> M1 & M2

    R1 & R2 & R3 --> AGG
    M1 & M2 --> AGG

    AGG --> DEC
    DEC --> AUD & EVT
```

## Technology Stack

### Runtime
- Java 21 (LTS)
- Spring Boot 3.2
- Spring Security OAuth2 Resource Server

### Data
- PostgreSQL 16 with per-service schemas
- Redis 7 for caching and sessions
- Flyway for migrations

### Messaging
- Redpanda (Kafka-compatible)
- JSON event encoding with schema versioning

### Observability
- Micrometer + Prometheus metrics
- OpenTelemetry tracing
- Loki for log aggregation
- Grafana dashboards

### Security
- Keycloak for identity
- OPA for policy decisions
- HashiCorp Vault for secrets

## Deployment Architecture

```mermaid
flowchart TB
    subgraph K8s["Kubernetes Cluster"]
        subgraph NS1["hafnium-backend"]
            IS[identity-service]
            SS[screening-service]
            MS[monitoring-service]
            CS[case-service]
            VS[vault-service]
            RS[risk-engine-service]
            SG[signals-service]
            AF[api-facade]
        end

        subgraph NS2["hafnium-data"]
            PG[(PostgreSQL)]
            RD[(Redis)]
            RP[Redpanda]
        end

        subgraph NS3["hafnium-infra"]
            KC[Keycloak]
            OPA[OPA]
            VT[Vault]
        end

        subgraph NS4["hafnium-observability"]
            PR[Prometheus]
            GR[Grafana]
            TM[Tempo]
            LK[Loki]
        end
    end

    ING[Ingress Controller] --> AF
    AF --> IS & SS & MS & CS & VS & RS & SG
```

## Configuration

Services are configured via environment variables with defaults for local development:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | localhost:9092 |
| `KEYCLOAK_ISSUER_URI` | Keycloak issuer | http://localhost:8081/realms/hafnium |
| `OPA_URL` | OPA server | http://localhost:8181 |
| `AI_INFERENCE_URL` | AI inference service | http://localhost:8001 |

## Quality Gates

- Code formatting: Spotless (Google Java Format)
- Static analysis: Error Prone
- Testing: JUnit 5 + Testcontainers
- Security: CodeQL, Trivy, OWASP Dependency Check
