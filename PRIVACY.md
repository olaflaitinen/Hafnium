# Privacy Policy and Data Handling

This document describes the data handling practices and privacy posture of the Hafnium platform.

---

## Overview

The Hafnium platform processes sensitive personal and financial data as part of its compliance and security functions. This document establishes the privacy principles and technical controls that govern data handling within the platform.

---

## Data Classification

### Classification Levels

| Level | Description | Examples |
|-------|-------------|----------|
| **PUBLIC** | Non-sensitive, publicly available | Documentation, open APIs |
| **INTERNAL** | Business-sensitive, internal use | Metrics, logs (redacted) |
| **CONFIDENTIAL** | Sensitive business data | Customer lists, transaction volumes |
| **RESTRICTED** | Highly sensitive, regulated | PII, financial data, KYC documents |

### Data Categories

| Category | Classification | Retention |
|----------|---------------|-----------|
| Customer PII | RESTRICTED | Per regulatory requirement |
| Transaction Data | RESTRICTED | 7 years (regulatory) |
| KYC Documents | RESTRICTED | Per regulatory requirement |
| Authentication Logs | CONFIDENTIAL | 2 years |
| Application Logs | INTERNAL | 90 days |
| Metrics | INTERNAL | 1 year |

---

## Privacy Principles

### Data Minimization

- Collect only data necessary for the stated purpose
- Avoid storing derived data when recomputation is feasible
- Implement automatic data expiration

### Purpose Limitation

- Data is processed only for documented purposes
- Secondary use requires explicit authorization
- Purpose changes require impact assessment

### Storage Limitation

- Data is retained only as long as necessary
- Automated deletion upon retention expiry
- Secure destruction of physical media

### Integrity and Confidentiality

- Encryption at rest and in transit
- Access controls based on least privilege
- Audit logging of all data access

---

## Technical Controls

### Encryption

| Data State | Encryption Standard |
|------------|---------------------|
| At Rest | AES-256-GCM |
| In Transit | TLS 1.3 |
| In Use | Secure enclave (where applicable) |

### Tokenization

Sensitive data is tokenized before storage:

| Data Type | Tokenization Method |
|-----------|---------------------|
| PAN (Card Numbers) | Format-preserving tokenization |
| SSN/Tax IDs | Random token with vault lookup |
| Bank Account Numbers | Encrypted reference token |

### Access Controls

| Control | Implementation |
|---------|----------------|
| Authentication | OAuth 2.0 / OIDC via Keycloak |
| Authorization | OPA policy-based access control |
| Audit | Immutable audit log with hash chain |

---

## Data Redaction

### Log Redaction

The following patterns are automatically redacted from logs:

| Pattern | Redaction |
|---------|-----------|
| Email addresses | `***@***.***` |
| Phone numbers | `***-***-****` |
| SSN | `***-**-****` |
| Credit card numbers | `****-****-****-XXXX` |
| IP addresses | `***.***.***.***` |

### API Response Redaction

Sensitive fields are masked in API responses based on caller permissions:

```json
{
  "customer_id": "cust_12345",
  "name": "John ***",
  "ssn": "***-**-1234",
  "email": "j***@example.com"
}
```

---

## AI and Machine Learning

### Training Data

- PII is excluded or anonymized in training datasets
- Synthetic data is used where possible
- Differential privacy techniques applied where applicable

### Model Inference

- Input data is not persisted beyond request lifecycle
- Model explanations do not leak training data
- Inference logs are anonymized

### LLM Integration

If LLM features are used:

- No PII is sent to external LLM providers
- Local redaction before API calls
- Response filtering for sensitive content

---

## Data Subject Rights

The platform supports the following data subject rights:

| Right | Implementation |
|-------|----------------|
| Access | Export API for customer data |
| Rectification | Update API with audit trail |
| Erasure | Secure deletion with confirmation |
| Portability | Standard export formats (JSON, CSV) |
| Restriction | Processing suspension flag |

---

## Incident Response

### Data Breach Response

1. **Detection**: Automated monitoring and alerting
2. **Containment**: Immediate access revocation
3. **Assessment**: Scope and impact analysis
4. **Notification**: Per regulatory requirements
5. **Remediation**: Root cause fix and hardening
6. **Documentation**: Incident report and lessons learned

### Reporting Obligations

| Jurisdiction | Notification Timeline |
|--------------|----------------------|
| EU (GDPR) | 72 hours |
| US (State Laws) | Varies by state |
| Financial Regulators | Per applicable regulations |

---

## Compliance Mapping

| Regulation | Relevant Sections |
|------------|-------------------|
| GDPR | Articles 5, 6, 17, 25, 32 |
| CCPA | 1798.100-1798.199 |
| PCI DSS | Requirements 3, 4, 7, 10 |
| SOC 2 | CC6, CC7 |

---

## Limitations

This document describes the technical privacy posture of the Hafnium platform. It does not constitute:

- Legal advice
- A privacy policy for end users
- Compliance certification

Organizations deploying Hafnium must:

1. Conduct their own privacy impact assessment
2. Develop user-facing privacy notices
3. Obtain necessary regulatory approvals
4. Implement jurisdiction-specific requirements

---

**DISCLAIMER**: This document is provided for informational purposes only and does not constitute legal advice. Organizations must consult with qualified legal counsel regarding privacy and data protection compliance.

*Last Updated: 2025-12-16*
