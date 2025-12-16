# Hafnium Architecture Overview

This document provides a high-level overview of the Hafnium platform architecture.

## System Overview

Hafnium is a unified RegTech and Cybersecurity platform built on an event-driven microservices architecture. The platform consists of four core engines:

1. **Hafnium KYC Flow**: Customer onboarding automation
2. **Hafnium AML Radar**: Anti-money laundering monitoring
3. **Hafnium Shield**: Fraud prevention and account security
4. **Hafnium Vault**: Data security and tokenization

## Architecture Diagram

```
                                    EXTERNAL CLIENTS
                                          |
                                          v
                              +---------------------+
                              |    API GATEWAY      |
                              |      (Envoy)        |
                              +---------------------+
                                          |
                    +---------------------+---------------------+
                    |                     |                     |
               +----v----+          +-----v-----+         +----v----+
               | Frontend|          |  Backend  |         |   Risk  |
               |  React  |          |   Java    |         |  Engine |
               +---------+          +-----------+         +---------+
                                          |                     |
                    +---------------------+---------------------+
                    |                     |                     |
               +----v----+          +-----v-----+         +----v----+
               |  Redis  |          | PostgreSQL|         | Redpanda|
               | (Cache) |          |   (DB)    |         | (Events)|
               +---------+          +-----------+         +---------+
                                          |
                    +---------------------+---------------------+
                    |                     |                     |
               +----v----+          +-----v-----+         +----v----+
               |  Vault  |          |   MinIO   |         |   AI    |
               |(Secrets)|          | (Storage) |         |Platform |
               +---------+          +-----------+         +---------+
```

## Component Layers

### Edge Layer

- **API Gateway (Envoy)**: TLS termination, routing, rate limiting
- **Frontend (React)**: User interface for analysts and operators

### Service Layer

- **Backend (Java/Spring Boot)**: Core business logic and REST APIs
- **Risk Engine (Python/FastAPI)**: Real-time risk scoring
- **Stream Processor (Kafka Streams)**: Event processing and enrichment

### Data Layer

- **PostgreSQL**: Primary relational database
- **Redis**: Caching and session storage
- **Redpanda**: Event streaming (Kafka-compatible)
- **MinIO**: Object storage for documents and models

### AI Layer

- **AI Platform**: Model training, registry, and inference
- **Feature Store (Feast)**: Feature management
- **Model Registry (MLflow)**: Model versioning and deployment

### Security Layer

- **Keycloak**: Identity and access management
- **OPA**: Policy-based authorization
- **Vault**: Secrets management

## Key Design Principles

1. **Event-Driven**: Loose coupling via asynchronous events
2. **AI-First**: ML models as first-class components
3. **Security by Design**: Zero-trust, encryption, tokenization
4. **Observability**: Metrics, logs, and traces for all components
5. **Compliance as Code**: Policies codified and version-controlled

## Further Reading

- [Data Architecture](data-architecture.md)
- [AI Platform](ai-platform.md)
- [Security Architecture](security.md)
- [Event Design](events.md)
