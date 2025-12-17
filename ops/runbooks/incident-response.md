# Incident Response Runbook

## Incident Severity Levels

| Level | Description | Response Time | Escalation |
|-------|-------------|---------------|------------|
| SEV1 | Complete outage or data breach | 15 minutes | Immediate page |
| SEV2 | Significant degradation | 30 minutes | Page on-call |
| SEV3 | Minor impact | 4 hours | Notification |
| SEV4 | No user impact | Next business day | Ticket |

---

## Triage Checklist

### 1. Initial Assessment

```
[ ] Identify affected services
[ ] Determine impact scope (users, tenants, regions)
[ ] Check error rate and latency dashboards
[ ] Review recent deployments
[ ] Check external dependencies
```

### 2. Communication

```
[ ] Create incident channel
[ ] Assign Incident Commander
[ ] Post initial status update
[ ] Notify stakeholders
```

### 3. Containment

```
[ ] Isolate affected components if needed
[ ] Enable circuit breakers
[ ] Scale up healthy replicas
[ ] Divert traffic if multi-region
```

---

## Service-Specific Procedures

### API Gateway (Envoy)

**Symptoms**: High latency, connection errors

**Checks**:
```bash
kubectl logs -n hafnium -l app=api-gateway --tail=100
kubectl get hpa -n hafnium
```

**Actions**:
- Scale replicas: `kubectl scale deployment api-gateway -n hafnium --replicas=5`
- Check upstream health: Review service mesh metrics

### Database (PostgreSQL)

**Symptoms**: Connection timeouts, slow queries

**Checks**:
```sql
SELECT * FROM pg_stat_activity WHERE state = 'active';
SELECT * FROM pg_locks WHERE NOT granted;
```

**Actions**:
- Kill long-running queries
- Check connection pool settings
- Review slow query log

### Kafka/Redpanda

**Symptoms**: Consumer lag, message delays

**Checks**:
```bash
rpk group describe <consumer-group>
rpk topic describe <topic>
```

**Actions**:
- Check broker health
- Increase consumer parallelism
- Review partition assignment

### ML Inference Service

**Symptoms**: High latency, fallback activation

**Checks**:
```bash
kubectl logs -n hafnium -l app=ai-inference --tail=100
curl http://ai-inference:8000/health
```

**Actions**:
- Verify model loaded correctly
- Check GPU/memory utilization
- Confirm rules-only fallback activated

---

## Rollback Procedure

### Kubernetes Rollback

```bash
# View rollout history
kubectl rollout history deployment/<service> -n hafnium

# Rollback to previous
kubectl rollout undo deployment/<service> -n hafnium

# Rollback to specific revision
kubectl rollout undo deployment/<service> -n hafnium --to-revision=<N>

# Verify rollback
kubectl rollout status deployment/<service> -n hafnium
```

### Database Migration Rollback

```bash
# Check current version
./gradlew :identity-service:flywayInfo

# Rollback (if undo scripts available)
./gradlew :identity-service:flywayUndo

# Manual intervention may be required for data migrations
```

---

## Post-Incident

### 1. Resolution Confirmation

```
[ ] All services healthy
[ ] Error rates at baseline
[ ] Latency at baseline
[ ] No duplicate alerts
```

### 2. Documentation

```
[ ] Timeline of events
[ ] Root cause identified
[ ] Impact quantified
[ ] Action items assigned
```

### 3. Post-Mortem

- Schedule within 48 hours
- Blameless analysis
- Focus on systemic improvements
- Track action items to completion

---

## Emergency Contacts

| Role | Escalation Path |
|------|-----------------|
| On-Call Engineer | PagerDuty |
| Engineering Manager | Slack + Phone |
| Security Team | security@hafnium.dev |
| Database Admin | DBA rotation |
