# Hafnium Runbooks

This directory contains operational runbooks for the Hafnium platform.

## Runbook Index

| Runbook | Description |
|---------|-------------|
| [Alert Response](alert-response.md) | Procedures for responding to monitoring alerts |
| [Incident Management](incident-management.md) | Incident classification and response procedures |
| [Database Operations](database-operations.md) | Database maintenance and recovery procedures |
| [Scaling](scaling.md) | Horizontal and vertical scaling procedures |
| [Disaster Recovery](disaster-recovery.md) | Disaster recovery and business continuity |

## Severity Levels

| Level | Response Time | Description |
|-------|---------------|-------------|
| SEV-1 | 15 minutes | Critical service outage affecting all users |
| SEV-2 | 1 hour | Partial outage or severe degradation |
| SEV-3 | 4 hours | Minor degradation or non-critical issues |
| SEV-4 | 24 hours | Low-impact issues or improvements |

## On-Call Responsibilities

1. Acknowledge pages within SLA
2. Assess severity and impact
3. Follow runbook procedures
4. Escalate as needed
5. Document actions and outcomes
6. Create post-incident report for SEV-1/SEV-2

## Common Procedures

### Checking Service Health

```bash
# Check all services
make logs

# Check specific service
docker compose logs -f risk-engine

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets
```

### Restarting Services

```bash
# Restart single service
docker compose restart risk-engine

# Restart all services
make restart
```

### Viewing Traces

1. Open Grafana at http://localhost:3001
2. Navigate to Explore
3. Select Tempo datasource
4. Search by trace ID or service name
