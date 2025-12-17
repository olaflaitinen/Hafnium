# Model Card Template

## Model Details

**Model ID**: [MODEL_ID]  
**Version**: [VERSION]  
**Type**: Binary Classification (Risk Scoring)  
**Framework**: scikit-learn GradientBoostingClassifier

### Intended Use

This model is designed to identify potentially suspicious transactions for AML/KYC compliance review. It produces a risk score between 0 and 1.

**Primary Use**: Transaction monitoring alert generation  
**Out-of-Scope**: Final decisions on customer relationships

---

## Training Data

**Dataset**: [DATASET_NAME]  
**Version**: [DATA_VERSION]  
**Size**: [N] samples  
**Label Distribution**: [X]% positive, [Y]% negative  
**Date Range**: [START] to [END]

### Features

| Feature | Type | Description |
|---------|------|-------------|
| transaction_count_30d | numeric | Transactions in last 30 days |
| avg_amount_30d | numeric | Average transaction amount |
| max_amount_30d | numeric | Maximum transaction amount |
| unique_counterparties | numeric | Unique counterparty count |
| account_age_days | numeric | Days since account opening |
| country_risk_score | numeric | Geographic risk factor |

---

## Performance Metrics

| Metric | Training | Validation | Test |
|--------|----------|------------|------|
| Accuracy | [X] | [Y] | [Z] |
| Precision | [X] | [Y] | [Z] |
| Recall | [X] | [Y] | [Z] |
| ROC-AUC | [X] | [Y] | [Z] |

---

## Fairness Analysis

Analysis performed across demographic segments:

| Segment | Precision | Recall | False Positive Rate |
|---------|-----------|--------|---------------------|
| [Group A] | [X] | [Y] | [Z] |
| [Group B] | [X] | [Y] | [Z] |

---

## Limitations

1. Model trained on historical data may not capture emerging patterns
2. Performance may vary across geographic regions not well represented in training data
3. Requires manual review of all alerts - not suitable for automated decisions

---

## Ethical Considerations

- Model outputs are advisory only and require human review
- No direct access to PII; features are pre-aggregated
- Bias monitoring should be performed quarterly
- Model should not be used for automated adverse actions

---

## Deployment

**Serving Infrastructure**: Kubernetes  
**Fallback**: Rule-based scoring if ML unavailable  
**Monitoring**: Prometheus metrics + drift detection

---

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Data Scientist | | | |
| Model Risk | | | |
| Compliance | | | |
