# Threat Model Overview

The Hafnium platform handles sensitive financial data and makes high-stakes automated decisions. Secure design is paramount.

---

## Methodology

We employ two complementary threat modeling methodologies:

1.  **STRIDE**: Focuses on technical threats to system components.
    [See STRIDE Analysis](stride.md)
2.  **LINDDUN**: Focuses on privacy threats to user data.
    [See LINDDUN Analysis](linddun.md)

---

## Assets at Risk

| Asset | Classification | Example | Impact |
|-------|----------------|---------|--------|
| **Customer PII** | Confidential | Name, Address, DoB | Identity Theft, Regulatory Fines |
| **Transaction Data** | Confidential | Amount, Counterparty | Financial Surveillance |
| **Risk Models** | Restricted | Model Weights, Logic | Circumvention of Controls |
| **Audit Logs** | Restricted | Decisions, Access | inability to demonstrate compliance |

---

## Attack Surface

### 1. Public API Gateway
The primary entry point for all external traffic. Hardened with WAF, Rate Limiting, and mTLS.

### 2. Supply Chain
Dependencies (Python/Java/Node packages) represent a significant vector. Controlled via Software Bill of Materials (SBOM) and signed releases.

### 3. Insider Threat
Privileged access by developers or operators. Mitigated via Least Privilege, MFA, and immutable audit logs.

---

## Critical Controls

- **Zero Trust Networking**: All inter-service communication is mutually authenticated (mTLS).
- **Data Encryption**: AES-256 for data at rest (DB/S3), TLS 1.3 for data in transit.
- **Defense in Depth**: Multiple layers (WAF -> Envoy -> OPA -> App Logic).
