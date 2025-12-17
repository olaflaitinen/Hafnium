# Service Level Indicators (SLIs)

This document defines the metrics used to measure service reliability.

---

## Metric Sources

| Source | Type | Collection |
|--------|------|------------|
| Prometheus | Metrics | Pull-based scraping |
| Grafana | Dashboards | Query Prometheus |
| Tempo | Traces | Push from OTel |
| Loki | Logs | Push from services |

---

## 1. Availability SLIs

### Request Success Rate

```promql
sum(rate(http_requests_total{status!~"5.."}[5m])) 
/ 
sum(rate(http_requests_total[5m]))
```

### Health Check Success

```promql
avg_over_time(up{job=~"hafnium-.*"}[5m])
```

---

## 2. Latency SLIs

### HTTP Request Duration

```promql
histogram_quantile(0.95, 
  sum(rate(http_request_duration_seconds_bucket[5m])) by (le, service)
)
```

### Transaction Processing Time

```promql
histogram_quantile(0.99,
  sum(rate(transaction_processing_duration_seconds_bucket[5m])) by (le)
)
```

### ML Inference Time

```promql
histogram_quantile(0.99,
  sum(rate(ml_inference_duration_seconds_bucket[5m])) by (le, model)
)
```

---

## 3. Throughput SLIs

### Transactions Per Second

```promql
sum(rate(transactions_ingested_total[5m]))
```

### Alerts Generated Per Hour

```promql
sum(increase(alerts_generated_total[1h]))
```

### KYC Workflows Completed Per Day

```promql
sum(increase(kyc_workflows_completed_total[24h]))
```

---

## 4. Error SLIs

### Error Rate by Service

```promql
sum(rate(http_requests_total{status=~"5.."}[5m])) by (service)
/
sum(rate(http_requests_total[5m])) by (service)
```

### Kafka Consumer Lag

```promql
sum(kafka_consumer_lag) by (consumer_group, topic)
```

### Database Connection Pool Saturation

```promql
hikaricp_connections_active / hikaricp_connections_max
```

---

## 5. Business SLIs

### False Positive Rate

```promql
sum(alerts_resolved{disposition="false_positive"})
/
sum(alerts_resolved)
```

### Mean Time to Case Resolution

```promql
histogram_quantile(0.50,
  sum(rate(case_resolution_duration_seconds_bucket[24h])) by (le)
)
```

---

## Dashboard Labels

All metrics should include these labels:

| Label | Description |
|-------|-------------|
| `service` | Service name |
| `environment` | dev/staging/prod |
| `tenant_id` | Tenant identifier |
| `version` | Application version |
