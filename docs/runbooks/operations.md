# Operations Runbook

**Scope**: Production Environment (Kubernetes)

---

## Deployment Strategy

We utilize **GitOps** using ArgoCD.
- **Source of Truth**: `infra/k8s/overlays/prod`
- **Sync Policy**: Automated with manual approval window for promotion.

---

## Routine Operations

### 1. Scaling Services
Autoscaling is enabled (HPA), but manual override is possible:

```bash
# Scale Risk Engine
kubectl scale deployment risk-engine -n hafnium --replicas=5
```

### 2. Database Maintenance
Vacuuming is automated. For manual failover:

```bash
# Trigger RDS failover (AWS)
aws rds reboot-db-instance \
    --db-instance-identifier hafnium-prod \
    --force-failover
```

### 3. Key Rotation
Secrets are managed in Vault. To rotate API keys:

1. Generate new key in upstream provider.
2. Update Vault path: `secret/hafnium/prod/api-keys`.
3. Restart pods to pick up new config (if not using dynamic reload):
    ```bash
    kubectl rollout restart deployment backend-api -n hafnium
    ```

---

## Monitoring & Alerting

### Key Dashboards
- **Global Overview**: System health, error rates, top-line metrics.
- **Risk Performance**: Inference latency, model drift (KL Divergence).
- **Business Ops**: Case volume, false positive rate, average handling time.

### Severity Levels

| Severity | Definition | Response Time (SLA) | Notification |
|----------|------------|---------------------|--------------|
| **SEV-1** | Critical Outage. Data Loss Risk. | 15 mins | PagerDuty (Call) |
| **SEV-2** | Substantial Degradation. | 30 mins | PagerDuty (Push) |
| **SEV-3** | Minor Issue. Workaround exists. | 4 hours | Slack / Email |
| **SEV-4** | Trivial / Cosmetic. | Next Business Day | Ticket |

---

## Backup & Restore

- **Database**: Continuous backups (WAL logs) + Daily Snapshots. Retention: 30 days.
- **Configuration**: Git version history.
- **State**: Kafka topic replication (7 days retention).

**Restore Drill Frequency**: Quarterly.
