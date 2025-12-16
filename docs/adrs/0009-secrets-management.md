# ADR-009: Secrets Management

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Security Team

## Context

The platform manages highly sensitive credentials (database passwords, API keys, encryption keys). These cannot be stored in Git or environment variables plain text.

## Decision

We selected **HashiCorp Vault**.

## Rationale

1. **Dynamic Secrets**: Vault can generate ephemeral database credentials that expire automatically.
2. **Centralization**: Unified interface for all secrets across K8s and legacy infra.
3. **Auditing**: Detailed access logs for every secret read.

## Consequences

- **Positive**: Elimination of long-lived static credentials; strong audit trail.
- **Negative**: High operational complexity (HA setup, unsealing).
