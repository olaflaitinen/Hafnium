# Hafnium Backend Architecture

## Overview

The Hafnium backend is a distributed system of Java microservices implementing a comprehensive AML/KYC compliance platform. The architecture follows domain-driven design principles with event-driven communication.

## System Context

```mermaid
C4Context
    title System Context - Hafnium Platform

    Person(analyst, "Compliance Analyst", "Reviews alerts and cases")
    Person(admin, "Administrator", "Configures system")
    
    System_Boundary(hafnium, "Hafnium Platform") {
        System(backend, "Backend Services", "Java microservices")
        System(frontend, "Web Application", "React dashboard")
        System(ai, "AI Services", "Python inference")
    }
    
    System_Ext(bank, "Core Banking", "Transaction source")
    System_Ext(sanctions, "Sanctions Lists", "OFAC, UN, EU")
    
    Rel(analyst, frontend, "Uses")
    Rel(frontend, backend, "API calls")
    Rel(backend, ai, "ML inference")
    Rel(bank, backend, "Transactions")
    Rel(backend, sanctions, "List updates")
```

## Service Architecture

```mermaid
graph TB
    subgraph "API Layer"
        GW[API Gateway]
        AF[api-facade]
    end
    
    subgraph "Domain Services"
        IS[identity-service]
        SS[screening-service]
        MS[monitoring-service]
        CS[case-service]
        VS[vault-service]
        RS[risk-engine-service]
        SG[signals-service]
    end
    
    subgraph "Platform Services"
        KC[Keycloak]
        OPA[OPA]
        RD[Redis]
    end
    
    subgraph "Data Layer"
        PG[(PostgreSQL)]
        KF[Kafka/Redpanda]
        MN[MinIO]
    end
    
    subgraph "AI Layer"
        AI[ai-inference]
        SP[stream-processor]
    end
    
    GW --> AF
    AF --> IS & SS & MS & CS & VS & RS & SG
    
    IS & SS & MS & CS & VS & RS & SG --> PG
    IS & SS & MS & CS & RS --> KF
    CS --> MN
    RS --> AI
    MS --> SP
    
    IS & SS & MS & CS & VS & RS & SG --> KC
    IS & SS & MS & CS & VS & RS & SG --> OPA
    IS & SS & MS & CS & VS & RS & SG --> RD
```

## Service Responsibilities

### identity-service
- Customer onboarding and lifecycle
- KYC workflow orchestration
- Document collection and verification
- Integration with AI for document analysis

### screening-service
- Sanctions and PEP list matching
- Fuzzy matching algorithms (Jaro-Winkler, Levenshtein)
- Match disposition workflow
- List import and update management

### monitoring-service
- Real-time transaction ingestion
- Rule engine evaluation
- Alert generation and prioritization
- Anomaly detection integration

### case-service
- Investigation case management
- Evidence collection and storage
- Workflow state machine
- AI-powered case summarization

### vault-service
- PII tokenization boundary
- Deterministic tokenization (HMAC)
- Encryption at rest (AES-GCM)
- Key management integration

### risk-engine-service
- Unified risk scoring
- Rule-based risk assessment
- ML model inference integration
- Decision persistence for audit

### signals-service
- Device fingerprint analysis
- Session risk evaluation
- Step-up authentication policies
- Behavioral analytics hooks

### api-facade
- Unified API surface for frontend
- Cross-service aggregation
- Consistent error handling
- Rate limiting and caching

## Data Flow

### Transaction Monitoring Flow

```mermaid
sequenceDiagram
    participant CB as Core Banking
    participant MS as monitoring-service
    participant RS as risk-engine-service
    participant SS as screening-service
    participant CS as case-service
    participant KF as Kafka
    
    CB->>MS: Ingest transaction
    MS->>RS: Calculate risk score
    RS-->>MS: Risk score + factors
    MS->>MS: Evaluate rules
    
    alt Alert triggered
        MS->>KF: alert.raised event
        MS->>SS: Screen counterparty
        SS-->>MS: Screening result
        
        alt High severity
            MS->>CS: Create case
            CS->>KF: case.created event
        end
    end
    
    MS->>KF: txn.ingested event
```

### KYC Workflow Flow

```mermaid
sequenceDiagram
    participant UI as Frontend
    participant IS as identity-service
    participant SS as screening-service
    participant RS as risk-engine-service
    participant AI as ai-inference
    participant KF as Kafka
    
    UI->>IS: Create customer
    IS->>KF: customer.created event
    
    UI->>IS: Initiate KYC
    IS->>KF: kyc.requested event
    
    UI->>IS: Upload documents
    IS->>AI: Verify document
    AI-->>IS: Verification result
    
    IS->>SS: Screen customer
    SS-->>IS: Screening result
    
    IS->>RS: Calculate risk tier
    RS-->>IS: Risk assessment
    
    IS->>IS: Complete KYC
    IS->>KF: customer.verified event
```

## Security Architecture

### Authentication & Authorization

```mermaid
graph LR
    subgraph "Client"
        UI[Frontend]
    end
    
    subgraph "Gateway"
        GW[API Gateway]
    end
    
    subgraph "Auth"
        KC[Keycloak]
        OPA[OPA]
    end
    
    subgraph "Services"
        SVC[Backend Service]
    end
    
    UI -->|1. Login| KC
    KC -->|2. JWT Token| UI
    UI -->|3. Request + JWT| GW
    GW -->|4. Forward| SVC
    SVC -->|5. Validate JWT| KC
    SVC -->|6. Check Policy| OPA
    OPA -->|7. Allow/Deny| SVC
```

### Multi-Tenancy

- Tenant ID extracted from JWT claims
- All queries scoped by tenant_id
- Row-Level Security (RLS) in PostgreSQL
- Tenant context propagated via ThreadLocal

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 15 |
| Messaging | Apache Kafka (Redpanda) |
| Cache | Redis |
| Auth | Keycloak |
| AuthZ | Open Policy Agent |
| Storage | MinIO |
| Observability | Prometheus, Grafana, Tempo |
