# Auditability & Traceability

This document defines the audit capabilities of the Hafnium platform to ensure full traceability of actions and decisions.

---

## Audit Architecture

All state-changing actions in the platform emit an immutable audit event to a dedicated robust changelog (Kafka topic `audit.logs` -> S3 WORM storage).

### Audit Event Schema

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-12-16T12:00:00Z",
  "actor": {
    "user_id": "u-12345",
    "role": "analyst",
    "ip_address": "10.0.0.1"
  },
  "action": {
    "resource": "case",
    "id": "c-98765",
    "operation": "update_status",
    "old_value": "open",
    "new_value": "closed"
  },
  "context": {
    "reason": "False positive verified"
  },
  "signature": "sha256:..."
}
```

---

## Traceability Matrix

| Action | Traceability Level | Log Location | Retention |
|--------|--------------------|--------------|-----------|
| **Login/Auth** | Full Session Trace | Keycloak Logs | 1 Year |
| **Data Access** | Read Access Log | API Gateway Logs | 1 Year |
| **Model Inference** | Input/Output/Version | MLflow / App Logs | 3 Years |
| **Configuration Change** | Diff + Approver | Git / Audit DB | Infinite |
| **Case Disposition** | User + Rationale | Database + Audit Log | 7 Years |

---

## Model Explainability

For every automated risk decision, the system persists:

1. **Model Version**: SHA of the model artifact.
2. **Input Vector**: Feature values at inference time.
3. **SHAP/LIME Values**: Contribution of each feature to the score.

This ensures that any automated decision can be reproduced and explained to regulators years after the fact.

---

## Integrity Protection

Audit logs are protected against tampering:

1. **Chained Hashing**: Each log entry includes the hash of the previous entry.
2. **Write-Once Storage**: S3 Object Lock in Governance mode.
3. **Access Control**: No single administrator has delete permissions for audit logs.
