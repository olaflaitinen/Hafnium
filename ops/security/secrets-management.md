# Secrets Management

This document describes the secrets management strategy for Hafnium.

---

## Principles

1. **No secrets in code**: All secrets externalized
2. **Least privilege**: Minimal access scope
3. **Rotation**: Regular automated rotation
4. **Audit**: All secret access logged
5. **Encryption**: At rest and in transit

---

## Secret Types

| Type | Examples | Storage | Rotation |
|------|----------|---------|----------|
| Database credentials | PostgreSQL password | External Secrets | 90 days |
| API keys | Third-party services | Vault | On demand |
| JWT signing keys | Keycloak realm keys | Keycloak | 30 days |
| Encryption keys | Data-at-rest keys | Vault Transit | Annual |
| Service tokens | Inter-service auth | Kubernetes | Auto |

---

## Local Development

```yaml
# .env.local (git-ignored)
POSTGRES_PASSWORD=local_dev_password
REDIS_PASSWORD=local_dev_password
KEYCLOAK_ADMIN_PASSWORD=admin
```

- Use `.env.local` for local secrets
- File is in `.gitignore`
- Use `docker compose --env-file .env.local`

---

## CI/CD (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
```

**Best Practices**:
- Use GitHub Secrets for CI/CD
- Use environment-specific secrets
- Pin action versions
- Prefer OIDC over long-lived tokens

---

## Kubernetes (Production)

### External Secrets Operator

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: hafnium-db-credentials
  namespace: hafnium
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
    kind: ClusterSecretStore
  target:
    name: hafnium-db-credentials
  data:
    - secretKey: password
      remoteRef:
        key: hafnium/database
        property: password
```

### Sealed Secrets (Alternative)

```bash
# Encrypt secret
kubeseal --format yaml < secret.yaml > sealed-secret.yaml

# Apply sealed secret
kubectl apply -f sealed-secret.yaml
```

---

## HashiCorp Vault Integration

```hcl
# Vault policy for Hafnium services
path "hafnium/data/*" {
  capabilities = ["read"]
}

path "hafnium/transit/encrypt/data-key" {
  capabilities = ["update"]
}

path "hafnium/transit/decrypt/data-key" {
  capabilities = ["update"]
}
```

---

## Rotation Procedures

### Database Password Rotation

1. Generate new password in Vault
2. Create new database user with new password
3. Update External Secret
4. Wait for pods to restart
5. Verify connectivity
6. Remove old database user

### JWT Key Rotation

1. Add new key to Keycloak (RS256)
2. Old key remains valid for token lifetime
3. After 2x token lifetime, remove old key

---

## Audit Logging

All secret access is logged:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "actor": "system:serviceaccount:hafnium:identity-service",
  "action": "read",
  "secret": "hafnium/database",
  "source_ip": "10.0.0.15"
}
```

---

## Emergency Procedures

### Suspected Compromise

1. Rotate all affected secrets immediately
2. Revoke compromised credentials
3. Audit access logs
4. Notify security team
5. Document in incident report

### Secret Recovery

- All secrets backed up to Vault
- Disaster recovery tested quarterly
- Recovery time objective: 1 hour
