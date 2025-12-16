# ADR-005: Saga Pattern for Distributed Transactions

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Backend Engineering Team  

## Context

The Hafnium platform performs multi-step operations that span multiple services:
1. Customer onboarding (create customer → KYC → screening → verification)
2. Alert processing (score → evaluate → alert → case creation)
3. Transaction processing (validate → enrich → score → persist)

Traditional distributed transactions (2PC) are impractical in a microservices architecture due to:
- Tight coupling between services
- Availability requirements
- Performance overhead
- Lack of support in modern message brokers

## Decision

We will implement the Saga pattern using choreography for simple flows and orchestration for complex flows.

### Choreography (Simple Flows)

Services react to events and emit subsequent events:

```
TransactionIngested → [Enrichment Service]
    → TransactionEnriched → [Scoring Service]
    → TransactionScored → [Alert Service]
    → AlertRaised (if applicable)
```

### Orchestration (Complex Flows)

A central orchestrator manages the saga state:

```
KYC Orchestrator
    → InitiateKYC
    → RequestDocuments
    → VerifyDocuments
    → RunScreening
    → FinalizeKYC
```

### Saga State Table

```sql
CREATE TABLE saga_state (
    saga_id UUID PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    current_step VARCHAR(50) NOT NULL,
    state JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

### Compensation

Each saga step has a compensating action:

| Step | Action | Compensation |
|------|--------|--------------|
| CreateCustomer | Insert record | Soft delete |
| RequestVerification | Start workflow | Cancel workflow |
| ChargeAccount | Debit funds | Credit refund |

## Rationale

### Alternatives Considered

1. **Two-Phase Commit (2PC)**: Blocking, poor availability
2. **TCC (Try-Confirm-Cancel)**: Complex, requires all services to support
3. **Eventual Consistency Only**: Insufficient for some operations

### Why Saga Pattern

| Requirement | Saga Solution |
|-------------|---------------|
| Availability | No distributed locks |
| Consistency | Compensation for rollback |
| Observability | Saga state tracking |
| Resilience | Retry and recovery built-in |

## Consequences

### Positive
- High availability (no blocking)
- Clear transaction boundaries
- Observable saga state
- Flexible compensation logic

### Negative
- Eventual consistency (not immediate)
- Complex compensation logic
- Increased implementation effort
- Potential for "orphaned" states

### Risks
- Compensation failures
- Saga state corruption
- Timeout handling complexity

### Mitigations
- Saga state monitoring dashboards
- Dead letter queues for failed steps
- Periodic saga state reconciliation
- Idempotent step implementations

## Implementation Notes

- Saga IDs propagated in event headers
- Steps are idempotent
- Timeouts trigger compensation
- Saga state queryable for debugging

## References

- Microservices Patterns (Chris Richardson)
- Saga Pattern - Microsoft Docs
- Eventual Consistency Pattern
