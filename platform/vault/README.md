# Vault Configuration

This directory contains HashiCorp Vault configuration for the Hafnium platform.

## Overview

Vault provides secrets management for:
- Database credentials
- API keys
- Encryption keys
- Service tokens

## Development Setup

### Start Vault (Dev Mode)

```bash
docker compose up vault -d
```

Development mode:
- Root token: `dev-root-token`
- Unsealed automatically
- In-memory storage (data lost on restart)

### Access Vault UI

Navigate to: http://localhost:8200

### CLI Access

```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='dev-root-token'

vault status
```

## Secret Paths

| Path | Description |
|------|-------------|
| `secret/data/hafnium/database` | Database credentials |
| `secret/data/hafnium/keycloak` | Keycloak admin credentials |
| `secret/data/hafnium/redis` | Redis credentials |
| `secret/data/hafnium/kafka` | Kafka credentials |
| `secret/data/hafnium/encryption` | Encryption keys |

## Policies

### Developer Policy

```hcl
path "secret/data/hafnium/*" {
  capabilities = ["read", "list"]
}
```

### Service Policy

```hcl
path "secret/data/hafnium/{{identity.entity.name}}/*" {
  capabilities = ["read"]
}
```

## Production Considerations

Production deployments require:

1. **Auto-unseal**: Configure cloud KMS for auto-unseal
2. **High Availability**: Deploy in HA mode with Raft or Consul
3. **Audit Logging**: Enable file or syslog audit backend
4. **Access Control**: Implement proper policies and namespaces
5. **Backup**: Configure automated snapshots

---

**WARNING**: Do not use development configuration in production.
