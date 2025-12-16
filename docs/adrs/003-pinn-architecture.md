# ADR-003: Physics-Informed Neural Network for Risk Scoring

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: AI Platform Team, Risk Engineering Team  

## Context

The risk scoring system requires models that are:

1. Accurate and performant
2. Interpretable for regulatory compliance
3. Robust against adversarial manipulation
4. Monotonic with respect to key risk indicators

Traditional ML models (XGBoost, Random Forests) lack inherent monotonicity guarantees, while simple rule-based systems lack representational power.

## Decision

We will implement a Physics-Informed Neural Network (PINN) architecture that encodes domain constraints as differentiable loss terms:

1. **Monotonicity Constraints**: Certain features must monotonically increase risk
2. **Smoothness Regularization**: Gradient penalties prevent adversarial sensitivity
3. **Calibration Loss**: Expected Calibration Error as differentiable objective

### Architecture

```
Input Features → Monotonic MLP → Risk Score [0,1]
                      ↓
              Physics-Informed Loss
              (monotonicity + smoothness + calibration)
```

### Loss Function

```python
L = L_supervised + λ₁L_monotonicity + λ₂L_smoothness + λ₃L_calibration
```

Where:

- L_supervised: Binary cross-entropy or focal loss
- L_monotonicity: Penalizes negative gradients for monotonic features
- L_smoothness: Gradient norm penalty
- L_calibration: Expected Calibration Error

## Rationale

### Alternatives Considered

1. **Standard Deep Learning**: Lacks interpretability and monotonicity
2. **Isotonic Regression**: Single feature, limited expressiveness
3. **Constrained Optimization (CVX)**: Not scalable to deep networks
4. **Post-hoc Calibration Only**: Doesn't address monotonicity

### Why PINN

| Requirement | PINN Solution |
|-------------|---------------|
| Accuracy | Deep learning expressiveness |
| Interpretability | Monotonicity as explanation |
| Robustness | Smoothness regularization |
| Calibration | Built into training objective |

## Consequences

### Positive

- Regulatory-compliant explainability ("higher X → higher risk")
- Reduced adversarial vulnerability
- Well-calibrated probability outputs
- Scientific grounding for risk calculations

### Negative

- More complex training procedure
- Hyperparameter tuning for loss weights
- Potentially slower convergence
- Requires domain expertise for constraint specification

### Risks

- Over-constraining may reduce accuracy
- Constraint specification errors
- Performance overhead in inference

### Mitigations

- Multi-phase training (supervised → constrained)
- Automated hyperparameter search
- Continuous monitoring of constraint satisfaction
- A/B testing against baseline models

## Implementation Notes

- Training uses separate loss phases
- Constraints are softened with temperature scaling
- Model cards document constraint specifications
- Benchmarking includes adversarial robustness tests

## References

- Rudin, C. (2019). Stop explaining black box models
- Liu & Wang (2020). Certified Monotonic Neural Networks
- PINN Literature: Raissi et al. (2019)
