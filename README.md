# Hafnium

[![CI](https://github.com/olaflaitinen/hafnium/actions/workflows/ci.yaml/badge.svg?branch=main)](https://github.com/olaflaitinen/hafnium/actions/workflows/ci.yaml)
[![Security](https://github.com/olaflaitinen/hafnium/actions/workflows/security.yaml/badge.svg?branch=main)](https://github.com/olaflaitinen/hafnium/actions/workflows/security.yaml)
[![Release Dry Run](https://github.com/olaflaitinen/hafnium/actions/workflows/release-dry-run.yaml/badge.svg)](https://github.com/olaflaitinen/hafnium/actions/workflows/release-dry-run.yaml)
[![Scorecard](https://api.securityscorecards.dev/projects/github.com/olaflaitinen/hafnium/badge)](https://securityscorecards.dev/viewer/?uri=github.com/olaflaitinen/hafnium)
[![License](https://img.shields.io/github/license/olaflaitinen/hafnium)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/commits/main)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/graphs/commit-activity)
[![Contributors](https://img.shields.io/github/contributors/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/graphs/contributors)
[![Open Issues](https://img.shields.io/github/issues/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/issues)
[![Closed Issues](https://img.shields.io/github/issues-closed/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/issues?q=is%3Aissue+is%3Aclosed)
[![Open PRs](https://img.shields.io/github/issues-pr/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/pulls)
[![Closed PRs](https://img.shields.io/github/issues-pr-closed/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/pulls?q=is%3Apr+is%3Aclosed)
[![Repo Size](https://img.shields.io/github/repo-size/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium)
[![Code Size](https://img.shields.io/github/languages/code-size/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium)
[![Top Language](https://img.shields.io/github/languages/top/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium)
[![Language Count](https://img.shields.io/github/languages/count/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium)
[![Stars](https://img.shields.io/github/stars/olaflaitinen/hafnium?style=social)](https://github.com/olaflaitinen/hafnium/stargazers)
[![Forks](https://img.shields.io/github/forks/olaflaitinen/hafnium?style=social)](https://github.com/olaflaitinen/hafnium/network/members)
[![Watchers](https://img.shields.io/github/watchers/olaflaitinen/hafnium?style=social)](https://github.com/olaflaitinen/hafnium/watchers)
[![Release](https://img.shields.io/github/v/release/olaflaitinen/hafnium?include_prereleases)](https://github.com/olaflaitinen/hafnium/releases)
[![GitHub tag](https://img.shields.io/github/v/tag/olaflaitinen/hafnium)](https://github.com/olaflaitinen/hafnium/tags)

---

## Overview

Hafnium is a unified regulatory technology (RegTech) and cybersecurity platform designed for financial institutions. The platform provides comprehensive capabilities for customer due diligence, transaction monitoring, fraud prevention, and data security operations.

---

## Platform Architecture

```mermaid
flowchart TB
    subgraph External["External Systems"]
        CORE["Core Banking"]
        SCREEN["Screening Providers"]
        GOV["Government APIs"]
    end

    subgraph Gateway["API Gateway Layer"]
        ENVOY["Envoy Proxy"]
        OPA["OPA Policy Engine"]
        KC["Keycloak"]
    end

    subgraph Services["Microservices"]
        BE["Backend Java"]
        RE["Risk Engine"]
        SP["Stream Processor"]
        AI["AI Platform"]
        AL["Audit Log"]
    end

    subgraph Data["Data Layer"]
        PG["PostgreSQL"]
        RD["Redis"]
        KF["Kafka"]
        ML["MLflow"]
    end

    subgraph Observability["Observability"]
        PROM["Prometheus"]
        GRAF["Grafana"]
        LOKI["Loki"]
        TEMPO["Tempo"]
    end

    CORE --> ENVOY
    SCREEN --> ENVOY
    GOV --> ENVOY
    ENVOY --> OPA
    OPA --> KC
    ENVOY --> BE
    ENVOY --> RE
    BE --> PG
    BE --> RD
    BE --> KF
    RE --> AI
    RE --> PG
    SP --> KF
    SP --> RE
    SP --> PG
    AI --> ML
    AL --> PG
    Services --> PROM
    PROM --> GRAF
    LOKI --> GRAF
    TEMPO --> GRAF
```

---

## Core Engines

| Engine | Purpose | Key Capabilities |
|--------|---------|------------------|
| **KYC Flow** | Customer Onboarding | Document verification, identity validation, risk assessment |
| **AML Radar** | Transaction Monitoring | Real-time scoring, pattern detection, alert generation |
| **Shield** | Fraud Prevention | Behavioral analytics, anomaly detection, session security |
| **Vault** | Data Security | Tokenization, encryption, access control, audit logging |

---

## Technology Stack

### Backend Services

| Component | Technology | Version |
|-----------|------------|---------|
| API Gateway | Envoy | 1.28+ |
| Authentication | Keycloak | 23+ |
| Authorization | OPA | 0.60+ |
| Backend | Spring Boot | 3.2+ |
| Risk Engine | FastAPI | 0.109+ |
| Stream Processor | Faust | 1.10+ |

### Data Infrastructure

| Component | Technology | Purpose |
|-----------|------------|---------|
| Database | PostgreSQL | Primary data store |
| Cache | Redis | Session and feature cache |
| Message Broker | Kafka (Redpanda) | Event streaming |
| ML Registry | MLflow | Model versioning |

### AI/ML Platform

| Component | Technology | Purpose |
|-----------|------------|---------|
| Core Framework | PyTorch | Model training |
| Architecture | PINN | Physics-informed risk scoring |
| Serving | BentoML | Model inference |
| Feature Store | Feast | Feature management |

---

## Risk Score Formulation

The unified risk score is computed using a Physics-Informed Neural Network with the following objective:

$$
\mathcal{L} = \mathcal{L}_{supervised} + \lambda_1 \mathcal{L}_{monotonicity} + \lambda_2 \mathcal{L}_{smoothness} + \lambda_3 \mathcal{L}_{calibration}
$$

Where:

- $\mathcal{L}_{supervised}$: Binary cross-entropy loss
- $\mathcal{L}_{monotonicity}$: Penalizes negative gradients for monotonic features
- $\mathcal{L}_{smoothness}$: Gradient norm regularization
- $\mathcal{L}_{calibration}$: Expected Calibration Error

---

## Service Level Objectives

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Latency (p95) | < 100ms | Prometheus histogram |
| API Latency (p99) | < 250ms | Prometheus histogram |
| Availability | 99.9% | Uptime monitoring |
| Error Rate | < 0.1% | Error count / total requests |
| Model Inference (p95) | < 50ms | Service metrics |

---

## Quick Start

### Prerequisites

- Docker 24.0+
- Docker Compose 2.20+
- Make 4.0+

### Development Setup

```bash
# Clone repository
git clone git@github.com:olaflaitinen/hafnium.git
cd hafnium

# Configure environment
cp .env.example .env

# Start development stack
make up

# Verify services
make health
```

### Service Endpoints

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | http://localhost:3000 | Web interface |
| Backend API | http://localhost:8080 | REST API |
| Risk Engine | http://localhost:8000 | Risk scoring |
| Keycloak | http://localhost:8081 | Authentication |
| Grafana | http://localhost:3001 | Monitoring |

---

## Documentation

| Category | Document | Description |
|----------|----------|-------------|
| Architecture | [Overview](docs/architecture/overview.md) | System architecture |
| Architecture | [Components](docs/architecture/components.md) | Service details |
| Architecture | [Data Model](docs/architecture/data-model.md) | Database schema |
| Security | [Threat Model](docs/threat-model/overview.md) | Security analysis |
| Compliance | [Overview](docs/compliance/overview.md) | Regulatory mapping |
| AI Platform | [PINN Specification](docs/ai/pinn-specification.md) | Model architecture |
| Operations | [Runbooks](docs/runbooks/operations.md) | Operations guides |
| Development | [Contributing](CONTRIBUTING.md) | Contribution guidelines |

---

## Project Structure

```
hafnium/
├── .github/                    # GitHub configuration
│   ├── workflows/              # CI/CD pipelines
│   └── ISSUE_TEMPLATE/         # Issue templates
├── contracts/                  # API specifications
│   ├── openapi/                # OpenAPI specs
│   ├── asyncapi/               # Event schemas
│   └── schemas/                # JSON schemas
├── docs/                       # Documentation
│   ├── architecture/           # Architecture docs
│   ├── ai/                     # AI platform docs
│   ├── compliance/             # Compliance docs
│   └── runbooks/               # Operations guides
├── infra/                      # Infrastructure
│   ├── k8s/                    # Kubernetes manifests
│   ├── helm/                   # Helm charts
│   └── terraform/              # Terraform configs
├── platform/                   # Platform services
│   ├── envoy/                  # API gateway
│   ├── keycloak/               # Authentication
│   └── policies/               # OPA policies
├── services/                   # Application services
│   ├── ai-platform/            # ML inference
│   ├── backend-java/           # Java backend
│   ├── frontend-react/         # React frontend
│   ├── risk-engine/            # Risk scoring
│   └── stream-processor/       # Event processing
└── scripts/                    # Development scripts
```

---

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## Security

For security vulnerability reporting, see [SECURITY.md](SECURITY.md).

---

## Disclaimer

This software is provided for informational purposes only and does not constitute legal, financial, or compliance advice. Organizations must conduct their own regulatory compliance assessments. See [LEGAL_DISCLAIMER.md](LEGAL_DISCLAIMER.md) for full terms.

---

*Copyright 2025 Hafnium Platform Team. All rights reserved.*
