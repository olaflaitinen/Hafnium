# Backend Security Controls

## Overview

This document describes the security controls implemented in the Hafnium backend services.

## Authentication

### JWT Token Validation

All services validate JWT tokens issued by Keycloak:

```java
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_AUTH_SERVER_URL}/realms/hafnium
          jwk-set-uri: ${KEYCLOAK_AUTH_SERVER_URL}/realms/hafnium/protocol/openid-connect/certs
```

### Token Claims

Required claims extracted from JWT:
- `sub` - User/service account ID
- `tenant_id` - Tenant identifier (custom claim)
- `realm_access.roles` - User roles

## Authorization

### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| `ADMIN` | Full access to all resources |
| `ANALYST` | Read access, case management |
| `OPERATOR` | Transaction ingestion, KYC |
| `SERVICE` | Service-to-service calls |
| `AUDITOR` | Read-only audit access |

### OPA Policies

Fine-grained authorization via Open Policy Agent:

```rego
package hafnium.authz

default allow = false

allow {
    input.user.roles[_] == "ADMIN"
}

allow {
    input.action == "read"
    input.user.roles[_] == "ANALYST"
    input.resource.tenant_id == input.user.tenant_id
}
```

## Multi-Tenancy

### Tenant Isolation

- Tenant ID propagated via JWT claims
- Thread-local context via `TenantContext`
- All database queries scoped by tenant_id
- Row-Level Security (RLS) enabled

### RLS Policies

```sql
CREATE POLICY tenant_isolation ON customers
    FOR ALL
    USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

## Data Protection

### PII Tokenization

Sensitive data tokenized via vault-service:

```java
// Tokenize before storage
String token = vaultClient.tokenize(Map.of("ssn", socialSecurityNumber));

// Detokenize when needed (audited)
String ssn = vaultClient.detokenize(token).get("ssn");
```

### Encryption

- Data at rest: AES-256-GCM
- Data in transit: TLS 1.3
- Key management: HashiCorp Vault

## Input Validation

### Request Validation

```java
public record CreateCustomerRequest(
    @NotBlank @Size(max = 255) String externalId,
    @NotNull CustomerType customerType,
    @Valid @Size(max = 100) Map<String, String> metadata
) {}
```

### SQL Injection Prevention

- JPA with parameterized queries
- No raw SQL concatenation
- Input sanitization

## Audit Logging

### Audit Events

All security-relevant actions logged:

```java
AuditEvent.create(
    tenantId,
    actorId,
    "customer.created",
    "Customer",
    customerId,
    details,
    previousHash
);
```

### Hash Chain Integrity

```java
// Each audit entry includes hash of previous entry
String entryHash = sha256(previousHash + tenantId + actorId + action + ...);
```

## Container Security

### Non-Root User

```dockerfile
RUN addgroup -g 1000 hafnium && adduser -u 1000 -G hafnium -D hafnium
USER hafnium
```

### Security Context

```yaml
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  readOnlyRootFilesystem: true
```

## Network Security

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-egress
spec:
  podSelector:
    matchLabels:
      tier: backend
  policyTypes:
    - Egress
  egress:
    - to:
        - podSelector:
            matchLabels:
              app: postgres
      ports:
        - protocol: TCP
          port: 5432
```

## Dependency Scanning

### OWASP Dependency Check

```bash
./gradlew dependencyCheckAnalyze
```

### Container Scanning

```bash
trivy image hafnium/identity-service:latest
```

## Secret Management

- No secrets in code or configuration files
- Kubernetes Secrets for sensitive values
- External Secrets Operator for production

## Vulnerability Response

| Severity | SLA |
|----------|-----|
| Critical | 24 hours |
| High | 7 days |
| Medium | 30 days |
| Low | 90 days |
