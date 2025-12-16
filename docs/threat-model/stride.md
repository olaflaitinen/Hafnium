# STRIDE Threat Analysis

**Methodology**: Microsoft STRIDE  
**Scope**: Core Platform Components

---

## 1. Spoofing Identity

**Threat**: An attacker impersonates a legitimate component (e.g., Risk Engine) to inject false data.

| Component | Architecture Threat | Mitigation | Status |
|-----------|---------------------|------------|--------|
| **Kafka** | Rogue producer injecting events | mTLS Authentication + ACLs | ✅ Implemented |
| **API** | User impersonation via token theft | Short-lived JWTs (15m) + Refesh rotation | ✅ Implemented |
| **DB** | App spoofing to bypass RLS | IAM Database Authentication | ⚠️ Planned |

---

## 2. Tampering with Data

**Threat**: Modification of data in transit or at rest.

| Component | Threat Scenario | Mitigation | Status |
|-----------|-----------------|------------|--------|
| **Audit Log** | Deleting evidence of fraud | WORM Storage (S3 Object Lock) | ✅ Implemented |
| **Model** | Adversarial perturbation of inputs | Input sanitization / Smoothness regularization | ✅ Implemented |
| **Database** | Direct SQL modification | Deployment via Migration Scripts only | ✅ Implemented |

---

## 3. Repudiation

**Threat**: An actor denies performing an action, and the system cannot prove otherwise.

| Component | Threat Scenario | Mitigation | Status |
|-----------|-----------------|------------|--------|
| **Case Mgmt** | Analyst approves fraud case | Signed Audit Entries with actor ID | ✅ Implemented |
| **API** | Client claims "I didn't send that" | Request Signing (HMAC) for critical ops | ⚠️ Planned |

---

## 4. Information Disclosure

**Threat**: Exposure of sensitive data to unauthorized parties.

| Component | Threat Scenario | Mitigation | Status |
|-----------|-----------------|------------|--------|
| **Logs** | PII leakage in error traces | Structured Logging + PII Redaction filters | ✅ Implemented |
| **S3** | Public bucket exposure | Block Public Access (Org Policy) | ✅ Implemented |
| **API** | Excessive data in response | Field-level filtering (GraphQL/DTOs) | ✅ Implemented |

---

## 5. Denial of Service

**Threat**: Degrading service availability for legitimate users.

| Component | Threat Scenario | Mitigation | Status |
|-----------|-----------------|------------|--------|
| **API** | HTTP Flood | Envoy Rate Limiting (Token Bucket) | ✅ Implemented |
| **Risk Engine** | Complex model query | Timeouts + Circuit Breakers | ✅ Implemented |
| **Kafka** | Disk filling | Retention policies by time/size | ✅ Implemented |

---

## 6. Elevation of Privilege

**Threat**: An unprivileged user gains privileged access.

| Component | Threat Scenario | Mitigation | Status |
|-----------|-----------------|------------|--------|
| **Kubernetes** | Container breakout | Pod Security Standards (Restricted) | ✅ Implemented |
| **App** | IDOR (Insecure Direct Object Ref) | OPA Policy (Tenant + User ownership check) | ✅ Implemented |
