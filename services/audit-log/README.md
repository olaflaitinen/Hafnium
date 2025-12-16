# Audit Log Service

This directory contains the audit log service for the Hafnium platform.

## Overview

The audit log service provides immutable, tamper-evident logging for compliance and security purposes.

## Features

- Immutable log entries with hash chaining
- Structured event format
- Database-backed persistence
- Export capabilities for regulatory reporting

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Services  │────▶│  Audit API  │────▶│  PostgreSQL │
└─────────────┘     └─────────────┘     └─────────────┘
                          │
                          ▼
                    ┌─────────────┐
                    │   Export    │
                    │   (S3/SIEM) │
                    └─────────────┘
```

## Event Schema

```json
{
  "event_id": "uuid",
  "event_type": "CUSTOMER_CREATED",
  "timestamp": "2025-12-16T12:00:00Z",
  "actor": {
    "type": "user",
    "id": "uuid",
    "tenant_id": "uuid"
  },
  "resource": {
    "type": "customer",
    "id": "uuid"
  },
  "action": "create",
  "outcome": "success",
  "metadata": {},
  "previous_hash": "sha256:...",
  "hash": "sha256:..."
}
```

## Usage

### Log an Event

```python
from hafnium_audit import AuditClient

client = AuditClient()
client.log_event(
    event_type="CUSTOMER_CREATED",
    actor=Actor(type="user", id=user_id),
    resource=Resource(type="customer", id=customer_id),
    action="create",
    outcome="success",
)
```

### Query Events

```python
events = client.query(
    resource_type="customer",
    resource_id=customer_id,
    start_time=datetime.now() - timedelta(days=30),
)
```

## Hash Chain Verification

```python
from hafnium_audit import verify_chain

is_valid = verify_chain(events)
assert is_valid, "Audit log tampering detected!"
```

---

**Implementation Status**: Planned

See database schema in `infra/postgres/init/001_schema.sql` for the audit_log table definition.
