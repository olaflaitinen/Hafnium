# Threat Model

This document describes the threat model for the Hafnium platform using STRIDE and LINDDUN methodologies.

---

## Overview

The Hafnium platform processes sensitive financial and personal data, making it a target for various threat actors. This threat model identifies potential threats and corresponding mitigations.

---

## System Boundary

### In Scope

- All Hafnium platform services
- Data stores (PostgreSQL, Redis, Kafka)
- Identity and access management (Keycloak)
- API Gateway (Envoy)
- AI/ML inference services

### Out of Scope

- Client applications (mobile apps, third-party integrations)
- Cloud provider infrastructure security
- Physical security

---

## Threat Actors

| Actor | Motivation | Capability |
|-------|------------|------------|
| External Attacker | Financial gain, disruption | High (APT-level) |
| Malicious Insider | Financial gain, revenge | Medium-High |
| Competitor | Corporate espionage | Medium |
| Script Kiddie | Notoriety | Low |
| Nation State | Intelligence | Very High |

---

## STRIDE Analysis

### Spoofing (S)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| S1 | Credential theft | MFA, short-lived tokens |
| S2 | Session hijacking | Secure cookies, token binding |
| S3 | API key compromise | Key rotation, scope limitation |
| S4 | Service impersonation | mTLS between services |

### Tampering (T)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| T1 | Request modification | TLS, request signing |
| T2 | Database tampering | Audit logs, checksums |
| T3 | Log tampering | Immutable logging, hash chains |
| T4 | Model tampering | Model signing, integrity checks |

### Repudiation (R)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| R1 | Denial of actions | Comprehensive audit logging |
| R2 | Timestamp manipulation | Synchronized clocks, signed logs |
| R3 | User action denial | Non-repudiable audit trail |

### Information Disclosure (I)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| I1 | Data breach | Encryption, access controls |
| I2 | Log exposure | Log redaction, access restriction |
| I3 | Error message leakage | Generic error responses |
| I4 | Model extraction | Rate limiting, watermarking |
| I5 | Side-channel attacks | Constant-time operations |

### Denial of Service (D)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| D1 | API flooding | Rate limiting, circuit breakers |
| D2 | Resource exhaustion | Resource quotas, auto-scaling |
| D3 | Algorithmic complexity | Input validation, timeouts |
| D4 | Database overload | Connection pooling, read replicas |

### Elevation of Privilege (E)

| Threat | Description | Mitigation |
|--------|-------------|------------|
| E1 | Tenant escape | RLS, tenant ID validation |
| E2 | Role escalation | RBAC, principle of least privilege |
| E3 | Container escape | Hardened runtime, non-root |
| E4 | SQL injection | Parameterized queries, ORM |

---

## LINDDUN Analysis (Privacy)

### Linkability (L)

| Threat | Mitigation |
|--------|------------|
| Correlation of user activities | Data minimization, pseudonymization |
| Cross-tenant data linking | Strict tenant isolation |

### Identifiability (I)

| Threat | Mitigation |
|--------|------------|
| Re-identification from anonymized data | k-anonymity, differential privacy |
| Identifier leakage in logs | Automatic log redaction |

### Non-Repudiation (N)

| Threat | Mitigation |
|--------|------------|
| Unwanted attribution | Purpose limitation, data retention limits |

### Detectability (D)

| Threat | Mitigation |
|--------|------------|
| Detection of user presence | Uniform response patterns |

### Disclosure of Information (D)

| Threat | Mitigation |
|--------|------------|
| Unauthorized data access | Access controls, encryption |
| Inference from ML models | Differential privacy in training |

### Unawareness (U)

| Threat | Mitigation |
|--------|------------|
| Lack of transparency | Privacy notices, data subject rights |

### Non-Compliance (N)

| Threat | Mitigation |
|--------|------------|
| Regulatory violations | Compliance monitoring, audits |

---

## Attack Trees

### Credential Theft Attack Tree

```
Goal: Steal User Credentials
├── Phishing Attack
│   ├── Email phishing
│   └── Credential harvesting site
├── Credential Stuffing
│   ├── Obtain leaked credentials
│   └── Automated login attempts
├── Session Hijacking
│   ├── XSS attack
│   └── Network interception
└── Insider Threat
    ├── Database access
    └── Log access
```

### Data Exfiltration Attack Tree

```
Goal: Exfiltrate Customer Data
├── Direct Database Access
│   ├── SQL injection
│   ├── Compromised credentials
│   └── Misconfigured access
├── API Abuse
│   ├── Broken access control
│   ├── BOLA/IDOR
│   └── Mass assignment
├── Model Extraction
│   ├── Query-based extraction
│   └── Side-channel analysis
└── Backup/Log Access
    ├── Unprotected backups
    └── Verbose logging
```

---

## Security Controls Summary

| Control Category | Controls |
|------------------|----------|
| Authentication | OAuth 2.0, MFA, JWT |
| Authorization | OPA policies, RBAC, RLS |
| Encryption | TLS 1.3, AES-256-GCM |
| Audit | Immutable logging, SIEM |
| Network | Network policies, WAF |
| Application | Input validation, SAST/DAST |

---

## Review Cadence

- **Full Review**: Annually
- **Incremental Review**: Per major release
- **Triggered Review**: After security incident

---

**DISCLAIMER**: This threat model provides a framework for security analysis. Organizations must conduct their own security assessments appropriate to their risk profile.

*Last Updated: 2025-12-16*
