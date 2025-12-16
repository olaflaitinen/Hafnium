# PINN Specification

**Physics-Informed Neural Network for Risk Scoring**

---

## Abstract

This specification defines the architecture, training procedure, and constraints for the Physics-Informed Neural Network (PINN) used in the Hafnium risk scoring system. The PINN encodes domain knowledge as differentiable constraints to ensure monotonicity, smoothness, and calibration.

---

## 1. Architecture

### 1.1 Network Structure

```
Input: x ∈ ℝᵈ (d features)
Hidden: h₁, h₂, ..., hₙ ∈ ℝʰ (n hidden layers, h units each)
Output: ŷ ∈ [0, 1] (risk probability)
```

### 1.2 Layer Definition

For layer l:

```
h_l = σ(W_l · h_{l-1} + b_l)
```

Where:

- W_l: Weight matrix
- b_l: Bias vector
- σ: Activation function (ReLU for hidden, Sigmoid for output)

### 1.3 Monotonicity Enforcement

For features requiring positive monotonicity:

```
W_l[i, j] ≥ 0 for monotonic input j
```

Implemented via weight clipping or exponential parameterization.

---

## 2. Loss Function

### 2.1 Total Loss

```
L = L_supervised + λ₁L_monotonicity + λ₂L_smoothness + λ₃L_calibration
```

### 2.2 Supervised Loss

Binary cross-entropy with class weights:

```
L_supervised = -1/N Σ[w_y · y·log(ŷ) + (1-y)·log(1-ŷ)]
```

### 2.3 Monotonicity Loss

Penalizes negative gradients for monotonic features:

```
L_monotonicity = 1/N Σ max(0, -∂ŷ/∂x_m)²
```

Where x_m is a monotonic feature.

### 2.4 Smoothness Loss

Gradient penalty for Lipschitz regularization:

```
L_smoothness = 1/N Σ (||∇_x ŷ||₂ - 1)²
```

### 2.5 Calibration Loss

Expected Calibration Error as differentiable loss:

```
L_calibration = Σ_{b=1}^{B} |B_b|/N |acc(B_b) - conf(B_b)|
```

Where B_b is the b-th confidence bin.

---

## 3. Training Procedure

### 3.1 Multi-Phase Training

| Phase | Epochs | Loss Components | Learning Rate |
|-------|--------|-----------------|---------------|
| Warm-up | 10 | L_supervised only | 1e-3 |
| Constraint Introduction | 50 | All losses (increasing λ) | 1e-4 |
| Fine-tuning | 40 | All losses (fixed λ) | 1e-5 |

### 3.2 Hyperparameters

| Parameter | Default | Range |
|-----------|---------|-------|
| Hidden layers | 3 | [2, 6] |
| Hidden units | 128 | [64, 512] |
| λ₁ (monotonicity) | 0.1 | [0.01, 1.0] |
| λ₂ (smoothness) | 0.01 | [0.001, 0.1] |
| λ₃ (calibration) | 0.1 | [0.01, 1.0] |
| Batch size | 256 | [64, 1024] |
| Learning rate | 1e-3 | [1e-5, 1e-2] |

### 3.3 Early Stopping

Stop training when:

- Validation AUC does not improve for 10 epochs
- Monotonicity violation rate > 1%

---

## 4. Feature Specification

### 4.1 Monotonic Features

| Feature | Monotonicity | Justification |
|---------|--------------|---------------|
| transaction_amount | Positive | Higher amounts → higher risk |
| velocity_1h | Positive | More activity → higher risk |
| velocity_24h | Positive | More activity → higher risk |
| failed_attempts | Positive | More failures → higher risk |
| days_active | Negative | Longer history → lower risk |
| avg_transaction_size | None | Not monotonic |

### 4.2 Feature Preprocessing

```python
# Standardization
x_scaled = (x - μ) / σ

# Monotonic feature direction correction
x_negative_monotonic = -x  # Flip sign for negative monotonicity
```

---

## 5. Validation Requirements

### 5.1 Performance Metrics

| Metric | Minimum Threshold |
|--------|-------------------|
| AUC-ROC | 0.85 |
| AUC-PR | 0.70 |
| KS Statistic | 0.50 |

### 5.2 Constraint Satisfaction

| Constraint | Threshold |
|------------|-----------|
| Monotonicity Violation Rate | < 0.1% |
| Expected Calibration Error | < 0.05 |
| Lipschitz Constant | < 10 |

### 5.3 Fairness Metrics

| Metric | Threshold |
|--------|-----------|
| Demographic Parity Difference | < 0.05 |
| Equalized Odds Difference | < 0.05 |

---

## 6. Inference

### 6.1 Input Validation

```python
def validate_input(x: np.ndarray) -> None:
    assert x.shape[-1] == n_features
    assert not np.isnan(x).any()
    assert not np.isinf(x).any()
```

### 6.2 Output Post-Processing

```python
# Temperature scaling for calibration
temperature = 1.5  # Learned on calibration set
score_calibrated = sigmoid(logits / temperature)
```

### 6.3 Inference Latency

| Environment | Target p99 Latency |
|-------------|-------------------|
| CPU | < 10ms |
| GPU | < 2ms |

---

## 7. Implementation Reference

See implementation in:

- `services/ai-platform/src/hafnium_ai/nn/pinn.py`

---

## References

1. Raissi, M., Perdikaris, P., & Karniadakis, G. E. (2019). Physics-informed neural networks.
2. Liu, J., & Nocedal, J. (2020). Certified Monotonic Neural Networks.
3. Guo, C., et al. (2017). On Calibration of Modern Neural Networks.

---

*Last Updated: 2025-12-16*
