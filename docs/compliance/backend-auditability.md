# Backend Auditability

## Overview

The Hafnium backend implements comprehensive audit logging for regulatory compliance. All security-relevant actions are logged with cryptographic integrity verification.

## Audit Log Structure

```sql
CREATE TABLE audit_log (
    log_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    details JSONB,
    previous_hash VARCHAR(64),
    entry_hash VARCHAR(64) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL
);
```

## Hash Chain Integrity

Each audit entry includes a SHA-256 hash chain for tamper detection:

```java
public record AuditEvent(
    UUID logId,
    UUID tenantId,
    String actorId,
    String actorType,
    String action,
    String resourceType,
    String resourceId,
    Map<String, Object> details,
    String previousHash,
    String entryHash,
    Instant createdAt
) {
    public static AuditEvent create(
        UUID tenantId,
        String actorId,
        String action,
        String resourceType,
        String resourceId,
        Map<String, Object> details,
        String previousHash
    ) {
        UUID logId = UUID.randomUUID();
        Instant now = Instant.now();
        
        String entryHash = sha256(
            previousHash + "|" +
            tenantId + "|" +
            actorId + "|" +
            action + "|" +
            resourceType + "|" +
            resourceId + "|" +
            now.toString()
        );
        
        return new AuditEvent(logId, tenantId, actorId, "user", 
            action, resourceType, resourceId, details, 
            previousHash, entryHash, now);
    }
}
```

## Audit Events

### Customer Events

| Action | Resource | Details |
|--------|----------|---------|
| `customer.created` | Customer | External ID, type |
| `customer.updated` | Customer | Changed fields |
| `customer.status_changed` | Customer | Old/new status |
| `customer.risk_tier_changed` | Customer | Old/new tier |

### KYC Events

| Action | Resource | Details |
|--------|----------|---------|
| `kyc.initiated` | KycWorkflow | Workflow type |
| `kyc.document_uploaded` | Document | Document type |
| `kyc.document_verified` | Document | Verification result |
| `kyc.completed` | KycWorkflow | Final status |

### Screening Events

| Action | Resource | Details |
|--------|----------|---------|
| `screening.requested` | ScreeningRequest | Entity details |
| `screening.match_found` | ScreeningMatch | List, score |
| `screening.match_resolved` | ScreeningMatch | Disposition |

### Case Events

| Action | Resource | Details |
|--------|----------|---------|
| `case.created` | Case | Type, priority |
| `case.assigned` | Case | Assignee |
| `case.status_changed` | Case | Old/new status |
| `case.evidence_added` | CaseEvidence | Evidence type |
| `case.closed` | Case | Resolution |

### Security Events

| Action | Resource | Details |
|--------|----------|---------|
| `auth.login` | Session | IP, device |
| `auth.logout` | Session | Duration |
| `auth.failed` | Session | Reason |
| `auth.stepup_required` | Session | Risk score |
| `authz.denied` | Resource | Policy, reason |

## Retention

| Category | Retention |
|----------|-----------|
| Security events | 7 years |
| Transaction events | 7 years |
| KYC events | 7 years |
| Case events | 10 years |
| Session events | 1 year |

## Integrity Verification

### Daily Verification

```bash
# Verify hash chain integrity
SELECT 
    log_id,
    CASE 
        WHEN entry_hash = sha256(
            previous_hash || '|' || 
            tenant_id::text || '|' ||
            actor_id || '|' ||
            action || '|' ||
            resource_type || '|' ||
            resource_id || '|' ||
            created_at::text
        ) THEN 'VALID'
        ELSE 'INVALID'
    END as integrity_status
FROM audit_log
WHERE created_at > NOW() - INTERVAL '1 day';
```

### Reporting

Audit reports generated for:
- Regulatory examinations
- Internal audits
- Incident investigations
- Compliance certifications

## Access Controls

### Who Can View Audits

| Role | Access |
|------|--------|
| AUDITOR | Read all audit logs |
| ADMIN | Read tenant audit logs |
| ANALYST | Read own actions only |

### Audit of Audit Access

All audit log queries are themselves logged.

## Regulatory Compliance

### BSA/AML

- Customer due diligence records
- Suspicious activity documentation
- Transaction monitoring evidence

### GDPR

- Data access records
- Consent logging
- Data deletion tracking

### SOC 2

- Authentication events
- Authorization decisions
- Configuration changes
