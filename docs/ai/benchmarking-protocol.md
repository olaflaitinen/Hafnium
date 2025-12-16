# AI Benchmarking Protocol

This protocol defines the methodology for evaluating model performance and validating improvements before deployment.

---

## 1. Evaluation Datasets

| Dataset | Size | Source | Purpose | Frequency |
|---------|------|--------|---------|-----------|
| **Golden Set** | 10k | Verified Historical | Regression Testing | Every Commit |
| **Challenger Set** | 50k | Recent Production | Performance Baselines | Daily |
| **Adversarial Set** | 5k | Synthetic Attacks | Robustness-Testing | Weekly |

*Note: All PII in datasets must be redacted or tokenized.*

---

## 2. Metrics & Thresholds

To be accepted, a candidate model must meet **all** gating criteria:

| Metric | Definition | Threshold (Gate) |
|--------|------------|------------------|
| **AUC-ROC** | Discriminative power | $\ge 0.92$ |
| **Precision@1%** | Top-tier precision | $\ge 0.85$ |
| **Recall (Fraud)** | Detection rate | $\ge 0.75$ |
| **Latency (p99)** | Inference speed | $\le 50ms$ |
| **Bias Parity** | Demographic parity | $\Delta \le 0.05$ |

---

## 3. Methodology

### Cross-Validation
- **Strategy**: Time-series split (rolling window).
- **Folds**: 5 folds.
- **Gap**: 7-day gap between train and test to prevent leakage.

### Bias Testing (Fairness)
Model performance is stratified by protected attributes (e.g., Age Bracket, Geo-Location) to ensure equal treatment.
- **Metric**: Equalized Odds.
- **Requirement**: The difference in FPR between any two groups must not exceed 5%.

---

## 4. Reporting

Automated benchmarking reports are generated in the CI pipeline.

```markdown
# Benchmark Report - Model v1.2.3

## Summary
- **Status**: PASSED
- **Date**: 2025-12-16
- **Commit**: `a1b2c3d`

## Metrics
| Metric | Baseline | Candidate | Delta |
|--------|----------|-----------|-------|
| AUC    | 0.915    | 0.922     | +0.7% |
| Latency| 45ms     | 48ms      | +3ms  |
```

---

## 5. Approval

Models passing the automated benchmark still require **human review** by a Lead Data Scientist and a Compliance Officer before promotion to the Staging environment.
