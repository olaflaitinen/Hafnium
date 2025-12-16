# Regulatory Policy Mapping

This document maps regulatory requirements to Hafnium platform controls.

---

## AML Directive Requirements

### Customer Due Diligence (CDD)

| Requirement | Hafnium Control | Component |
|-------------|-----------------|-----------|
| Customer identification | KYC workflow | Backend Java |
| Beneficial owner identification | UBO collection | Backend Java |
| Risk assessment | Risk scoring | Risk Engine |
| Ongoing monitoring | Transaction monitoring | Stream Processor |

### Suspicious Activity Reporting

| Requirement | Hafnium Control | Component |
|-------------|-----------------|-----------|
| Alert generation | Rule engine | Stream Processor |
| Case management | Investigation workflow | Backend Java |
| SAR filing | Export functionality | Backend Java |
| Record retention | Audit logging | All services |

---

## GDPR Requirements

### Article 5: Data Processing Principles

| Principle | Hafnium Control |
|-----------|-----------------|
| Lawfulness | Purpose documentation |
| Fairness | Transparency in processing |
| Transparency | Privacy notices |
| Purpose limitation | Data classification |
| Data minimization | Field-level access control |
| Accuracy | Data validation |
| Storage limitation | Retention policies |
| Integrity | Encryption, access controls |
| Confidentiality | Encryption, access controls |
| Accountability | Audit logging |

### Article 25: Data Protection by Design

| Requirement | Hafnium Control |
|-------------|-----------------|
| Privacy by design | Tokenization, encryption |
| Privacy by default | Minimal data collection |

### Article 32: Security of Processing

| Requirement | Hafnium Control |
|-------------|-----------------|
| Encryption | TLS 1.3, AES-256 |
| Confidentiality | Access controls |
| Integrity | Audit logging |
| Availability | High availability architecture |
| Testing | Security scanning in CI |

---

## PCI DSS Requirements

### Requirement 3: Protect Stored Data

| Control | Hafnium Implementation |
|---------|------------------------|
| 3.4 Render PAN unreadable | Tokenization |
| 3.5 Protect encryption keys | Vault integration |
| 3.6 Key management | Key rotation procedures |

### Requirement 4: Encrypt Transmission

| Control | Hafnium Implementation |
|---------|------------------------|
| 4.1 Strong cryptography | TLS 1.3 |
| 4.2 Never send PAN unencrypted | API gateway enforcement |

### Requirement 7: Restrict Access

| Control | Hafnium Implementation |
|---------|------------------------|
| 7.1 Limit access | RBAC, OPA policies |
| 7.2 Access control system | Keycloak, OPA |

### Requirement 10: Track and Monitor

| Control | Hafnium Implementation |
|---------|------------------------|
| 10.1 Audit trails | Immutable audit log |
| 10.2 Automated audit trails | Application logging |
| 10.3 Record audit trail entries | Structured logging |

---

## SOC 2 Criteria

### CC6: Logical and Physical Access Controls

| Criterion | Hafnium Control |
|-----------|-----------------|
| CC6.1 Logical access security | Keycloak authentication |
| CC6.2 Authentication | OAuth 2.0, MFA |
| CC6.3 Access authorization | OPA policies |
| CC6.6 Access restrictions | Role-based access |
| CC6.7 Access changes | Provisioning workflow |
| CC6.8 Access removal | Deprovisioning workflow |

### CC7: System Operations

| Criterion | Hafnium Control |
|-----------|-----------------|
| CC7.1 Vulnerability management | Security scanning |
| CC7.2 Anomaly detection | Monitoring, alerting |
| CC7.3 Security events | SIEM integration |
| CC7.4 Incident response | Runbooks |

---

## Control Implementation Status

| Control Area | Status | Notes |
|--------------|--------|-------|
| Authentication | Implemented | Keycloak |
| Authorization | Implemented | OPA |
| Encryption at rest | Implemented | PostgreSQL |
| Encryption in transit | Implemented | TLS |
| Audit logging | Implemented | Immutable logs |
| Access controls | Implemented | RBAC, RLS |
| Vulnerability scanning | Implemented | CI pipeline |
| Incident response | Documented | Runbooks |

---

**DISCLAIMER**: This mapping is provided for reference only and does not constitute compliance certification. Organizations must validate controls against specific regulatory requirements.

*Last Updated: 2025-12-16*
