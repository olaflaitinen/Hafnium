# Service Level Objectives (SLOs)

This document defines the Service Level Objectives for the Hafnium platform.

## Overview

SLOs are reliability targets that balance feature velocity with system reliability. They are derived from SLIs (Service Level Indicators) and inform error budget decisions.

---

## 1. API Availability

**SLO**: 99.9% of API requests complete successfully (non-5xx) over a rolling 30-day window.

**SLI**: `sum(rate(http_requests_total{status!~"5.."}[5m])) / sum(rate(http_requests_total[5m]))`

**Error Budget**: 43.2 minutes of downtime per 30 days

| Tier | Target | Measurement Window |
|------|--------|-------------------|
| Critical Path | 99.95% | 30 days |
| Standard | 99.9% | 30 days |
| Best Effort | 99.5% | 30 days |

---

## 2. API Latency

**SLO**: 95% of requests complete within 500ms; 99% within 2000ms.

**SLI**: `histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`

| Endpoint Category | P50 | P95 | P99 |
|-------------------|-----|-----|-----|
| Read operations | 50ms | 200ms | 500ms |
| Write operations | 100ms | 500ms | 1000ms |
| Batch operations | 500ms | 2000ms | 5000ms |

---

## 3. Transaction Processing Latency

**SLO**: 99% of transactions are processed (enriched + scored) within 5 seconds.

**SLI**: `histogram_quantile(0.99, rate(transaction_processing_duration_seconds_bucket[5m]))`

**Components**:
- Ingestion: < 100ms
- Enrichment: < 500ms
- Scoring: < 1000ms
- Rule evaluation: < 500ms
- Alert generation: < 200ms

---

## 4. Alert Generation Accuracy

**SLO**: False positive rate below 15% for high-severity alerts.

**SLI**: `(alerts_resolved_as_false_positive{severity="high"}) / (alerts_total{severity="high"})`

---

## 5. KYC Workflow Completion

**SLO**: 95% of automated KYC workflows complete within 24 hours.

**SLI**: Time from workflow initiation to completion.

---

## 6. ML Inference Latency

**SLO**: 99% of ML inference requests complete within 100ms.

**SLI**: `histogram_quantile(0.99, rate(ml_inference_duration_seconds_bucket[5m]))`

**Fallback Activation**: If ML service unavailable, rules-only scoring activates within 500ms.

---

## 7. Data Freshness

**SLO**: Screening lists updated within 4 hours of source publication.

**SLI**: `time() - last_list_update_timestamp`

---

## Error Budget Policy

| Budget Remaining | Actions |
|------------------|---------|
| > 50% | Normal development velocity |
| 25-50% | Focus on reliability improvements |
| 10-25% | Feature freeze, reliability work only |
| < 10% | Incident response mode |

---

## Alerting Thresholds

| Metric | Warning | Critical |
|--------|---------|----------|
| Error rate | > 1% | > 5% |
| P99 latency | > 1s | > 3s |
| Error budget burn | 2x normal | 10x normal |
