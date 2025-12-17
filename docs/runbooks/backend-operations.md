# Backend Operations Runbook

## Service Health Checks

### Quick Health Check

```bash
# Check all services
for svc in identity screening monitoring case vault risk-engine signals api-facade; do
  curl -s http://localhost:808x/actuator/health | jq '.status'
done

# Individual service
curl http://localhost:8081/actuator/health | jq
```

### Kubernetes

```bash
# Pod status
kubectl get pods -n hafnium -l tier=backend

# Service endpoints
kubectl get endpoints -n hafnium

# Logs
kubectl logs -n hafnium -l app=identity-service --tail=100
```

## Common Issues

### Service Won't Start

**Symptoms**: Pod in CrashLoopBackOff, application fails to boot

**Diagnosis**:
```bash
kubectl logs -n hafnium <pod-name> --previous
kubectl describe pod -n hafnium <pod-name>
```

**Common causes**:
1. Database connection failed → Check `SPRING_DATASOURCE_URL`
2. Keycloak unreachable → Verify Keycloak is running
3. OOM → Increase memory limits

### High Latency

**Symptoms**: API response times > 500ms

**Diagnosis**:
```bash
# Check Prometheus metrics
curl localhost:8081/actuator/prometheus | grep http_server_requests

# Database connection pool
curl localhost:8081/actuator/metrics/hikaricp.connections.active | jq
```

**Remediation**:
1. Scale up replicas: `kubectl scale deployment identity-service --replicas=3`
2. Check database slow queries
3. Verify Redis cache hit rate

### Kafka Consumer Lag

**Symptoms**: Events not being processed, lag increasing

**Diagnosis**:
```bash
# Check consumer groups
rpk group describe identity-service

# Check lag metrics
curl localhost:8081/actuator/prometheus | grep kafka_consumer_lag
```

**Remediation**:
1. Increase consumer threads
2. Scale up consumer pods
3. Check for poison messages in DLQ

## Database Operations

### Flyway Migrations

```bash
# Check migration status
./gradlew :identity-service:flywayInfo

# Run migrations
./gradlew :identity-service:flywayMigrate

# Repair (after failed migration)
./gradlew :identity-service:flywayRepair
```

### Connection Issues

```bash
# Test connectivity
psql -h $DB_HOST -U hafnium -d hafnium -c "SELECT 1"

# Check active connections
SELECT count(*) FROM pg_stat_activity WHERE datname='hafnium';

# Kill idle connections
SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE datname='hafnium' AND state='idle' AND query_start < now() - interval '1 hour';
```

## Scaling

### Horizontal Pod Autoscaler

```bash
# View HPA status
kubectl get hpa -n hafnium

# Manual scale
kubectl scale deployment identity-service -n hafnium --replicas=5

# Update HPA limits
kubectl patch hpa identity-service-hpa -n hafnium -p '{"spec":{"maxReplicas":20}}'
```

## Disaster Recovery

### Service Restart

```bash
# Rolling restart
kubectl rollout restart deployment/identity-service -n hafnium

# Check rollout status
kubectl rollout status deployment/identity-service -n hafnium
```

### Rollback

```bash
# View history
kubectl rollout history deployment/identity-service -n hafnium

# Rollback to previous
kubectl rollout undo deployment/identity-service -n hafnium

# Rollback to specific revision
kubectl rollout undo deployment/identity-service -n hafnium --to-revision=3
```

## Monitoring Alerts

| Alert | Severity | Action |
|-------|----------|--------|
| ServiceDown | Critical | Page on-call, check pods |
| HighLatencyP99 | Warning | Check database, scale up |
| KafkaLagHigh | Warning | Check consumers, DLQ |
| MemoryPressure | Warning | Check for leaks, scale up |
| CertificateExpiring | Warning | Renew certificates |

## Emergency Contacts

| Role | Contact |
|------|---------|
| On-Call Engineer | PagerDuty |
| Database Admin | DBA Team |
| Security | SecOps Team |
