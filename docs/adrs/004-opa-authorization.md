# ADR-004: OPA for Policy-Based Authorization

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Security Team, Platform Engineering Team  

## Context

The Hafnium platform requires fine-grained authorization that:
1. Enforces role-based access control (RBAC)
2. Implements tenant isolation
3. Supports attribute-based decisions (risk level, time-based access)
4. Provides audit trail for compliance
5. Can be tested and version-controlled

Traditional approaches (hardcoded checks, ACLs) are insufficient for complex compliance requirements.

## Decision

We will adopt Open Policy Agent (OPA) as the centralized policy decision point using Rego policy language.

### Architecture

```
Request → Envoy Gateway → OPA Sidecar → Decision
                              ↓
                         Policy Bundle
                         (version controlled)
```

### Policy Structure

```
platform/policies/
├── authz.rego          # Main authorization policy
├── rbac.rego           # Role definitions
├── tenant.rego         # Tenant isolation rules
├── data_access.rego    # Field-level access control
└── test/               # Policy unit tests
```

### Decision Input

```json
{
  "subject": {
    "user_id": "uuid",
    "tenant_id": "uuid",
    "roles": ["analyst"],
    "attributes": {}
  },
  "resource": {
    "type": "customer",
    "id": "uuid",
    "tenant_id": "uuid"
  },
  "action": "read"
}
```

## Rationale

### Alternatives Considered

1. **Application-Embedded Authorization**: Scattered, hard to audit
2. **Casbin**: Less expressive than Rego for complex policies
3. **AWS IAM-style Policies**: Vendor lock-in, less flexible
4. **Custom Policy Engine**: Maintenance burden, wheel reinvention

### Why OPA

| Criterion | OPA Advantage |
|-----------|---------------|
| Expressiveness | Rego handles complex logic |
| Testability | Built-in testing framework |
| Performance | In-memory, sub-millisecond decisions |
| Ecosystem | Wide adoption, Envoy integration |
| Auditability | Decision logging, policy versioning |

## Consequences

### Positive
- Centralized, version-controlled policies
- Testable authorization logic
- Separation of policy from code
- Consistent enforcement across services
- Audit-ready decision logging

### Negative
- Learning curve for Rego language
- Additional infrastructure component
- Potential single point of failure
- Policy deployment coordination

### Risks
- Policy errors could block legitimate access
- Performance impact at high request rates
- Complexity in policy debugging

### Mitigations
- Comprehensive policy testing in CI
- Canary deployments for policy changes
- OPA decision caching
- Fallback behavior on OPA unavailability

## Implementation Notes

- Policies bundled and deployed via CI/CD
- Decision logs exported to audit system
- Integration tests validate policy behavior
- Policy changes require security review

## References

- Open Policy Agent Documentation
- NIST ABAC Guide
- OPA Rego Testing Documentation
