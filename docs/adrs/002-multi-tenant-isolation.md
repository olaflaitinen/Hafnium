# ADR-002: Multi-Tenant Data Isolation

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Platform Engineering Team  

## Context

The Hafnium platform serves multiple financial institutions (tenants) from a shared infrastructure. Each tenant's data must be strictly isolated to comply with regulatory requirements and prevent unauthorized access between tenants.

## Decision

We will implement tenant isolation using a combination of:

1. **Database-Level Isolation**: PostgreSQL Row-Level Security (RLS) policies that automatically filter data by tenant_id
2. **Application-Level Enforcement**: Tenant context propagation through JWT claims
3. **Network-Level Isolation**: OPA policies enforcing tenant boundaries

### Implementation Details

**Database RLS**:

```sql
CREATE POLICY tenant_isolation ON customers
  USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

**JWT Claims**:

```json
{
  "tenant_id": "uuid",
  "roles": ["analyst"],
  "scope": ["read:customers"]
}
```

**OPA Policy**:

```rego
allow {
  input.resource.tenant_id == input.subject.tenant_id
}
```

## Rationale

### Alternatives Considered

1. **Separate Databases per Tenant**: Rejected due to operational complexity and cost
2. **Schema-per-Tenant**: Rejected due to migration complexity at scale
3. **Application-Only Enforcement**: Rejected as insufficient for compliance

### Trade-offs

| Approach | Pros | Cons |
|----------|------|------|
| RLS | Defense in depth, automatic | PostgreSQL-specific |
| Separate DBs | Complete isolation | High operational cost |
| App-only | Simple implementation | Single point of failure |

## Consequences

### Positive

- Strong isolation with defense in depth
- Simplified application logic (RLS is automatic)
- Audit trail at database level
- Compliance with data residency requirements

### Negative

- PostgreSQL lock-in for RLS features
- Slight performance overhead for RLS policies
- Complexity in cross-tenant analytics (requires explicit bypass)

### Risks

- Misconfigured RLS could expose data
- Performance degradation at extreme scale

### Mitigations

- Automated RLS policy testing
- Performance benchmarking in CI
- Regular security audits

## References

- PostgreSQL RLS Documentation
- OWASP Multi-Tenancy Security Cheat Sheet
