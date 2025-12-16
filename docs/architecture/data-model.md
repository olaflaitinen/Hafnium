# Data Model Specification

This document defines the core entity relationship model and schema standards for the Hafnium platform.

---

## Entity Relationship Diagram

```mermaid
erDiagram
    TENANT ||--o{ CUSTOMER : owns
    TENANT ||--o{ USER : employs
    CUSTOMER ||--o{ TRANSACTION : initiates
    CUSTOMER ||--o{ ACCOUNT : holds
    CUSTOMER ||--o{ CASE : subject_of
    TRANSACTION ||--o{ ALERT : triggers
    ALERT }|--|| CASE : grouped_into
    CASE ||--o{ SAR_REPORT : results_in
    USER ||--o{ CASE : investigates

    TENANT {
        uuid id PK
        string name
        jsonb config
    }
    
    CUSTOMER {
        uuid id PK
        uuid tenant_id FK
        string external_id
        enum status
        float risk_score
        timestamp created_at
    }
    
    TRANSACTION {
        uuid id PK
        uuid customer_id FK
        decimal amount
        string currency
        timestamp timestamp
        jsonb metadata
    }
    
    ALERT {
        uuid id PK
        uuid txn_id FK
        string rule_id
        enum severity
    }

    CASE {
        uuid id PK
        enum state
        uuid assignee_id FK
    }
```

---

## Case Lifecycle State Machine

```mermaid
stateDiagram-v2
    [*] --> Open : Alert Triggered
    Open --> Investigating : Analyst Assigned
    
    state Investigating {
        [*] --> Review_Evidence
        Review_Evidence --> Request_Info : Need More Data
        Request_Info --> Review_Evidence : Info Received
    }
    
    Investigating --> Closed_FalsePositive : Beneign
    Investigating --> Escalated_SAR : Suspicious
    
    Escalated_SAR --> SAR_Filed : MLRO Approval
    Escalated_SAR --> Closed_FalsePositive : MLRO Rejection
    
    SAR_Filed --> [*]
    Closed_FalsePositive --> [*]
```

---

## Schema Definitions

### Database Standards

- **Dialect**: PostgreSQL 15+
- **Primary Keys**: UUID v4 (Random)
- **Timezones**: UTC always (`TIMESTAMPTZ`)
- **Strings**: `TEXT` (no arbitrary `VARCHAR(n)` limits unless strictly required)
- **Money**: `DECIMAL(19, 4)` for high precision currency

### Core Tables

#### 1. Customers (`customers`)

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | No | Primary Key |
| `tenant_id` | UUID | No | Multi-tenancy isolation key |
| `external_id` | TEXT | No | ID in upstream banking core |
| `risk_tier` | ENUM | No | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `kyc_status` | ENUM | No | `PENDING`, `VERIFIED`, `REJECTED` |

#### 2. Transactions (`transactions`)

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | No | Primary Key |
| `account_id` | UUID | No | Source account |
| `amount` | DECIMAL | No | Transaction value |
| `currency` | CHAR(3) | No | ISO 4217 Currency Code |
| `counterparty` | JSONB | Yes | Receiver details (PII redacted) |

---

## Data Lifecycle

| Entity | Retention Period | Archival Strategy | Deletion Policy |
|--------|------------------|-------------------|-----------------|
| Transaction | 5 years | Cold storage after 1 year | Hard delete (GDPR) |
| Audit Log | 7 years | WORM storage (S3 Object Lock) | Never (Regulatory) |
| Customer PII | Contract + 2y | Encrypted at rest | Crypto-shredding |
| Alert | 3 years | Summarized after 6 months | Bulk purge |

---

## Data Classification

| Level | Definition | Examples | Handling |
|-------|------------|----------|----------|
| **Public** | Non-sensitive | API Specs, Public Keys | No specific controls |
| **Internal** | Business operations | Internal IDs, Metadata | AuthZ required |
| **Confidential** | PII / Financial | Name, Address, Txn Amount | Encryption at rest, Need-to-know |
| **Restricted** | Critical Secrets | Private Keys, Passwords | Hardware security module (HSM) |

---

## Feature Store Schema

For AI models, features are defined in the online store (Redis):

| Feature Name | Type | Window | Description |
|--------------|------|--------|-------------|
| `txn_velocity_1h` | Int64 | 1 hour | Count of txns in last hour |
| `txn_velocity_24h` | Int64 | 24 hours | Count of txns in last day |
| `sum_amount_7d` | Float | 7 days | Total volume in last week |

Feature keys follow the pattern: `entity:{entity_id}:feature:{feature_name}`.
