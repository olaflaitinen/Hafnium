# Hafnium AI Platform

Production-grade AI/ML platform for risk scoring, anomaly detection, and document verification.

## Components

| Component | Technology | Purpose |
|-----------|------------|---------|
| Training | Prefect | Orchestrated training pipelines |
| Feature Store | Feast | Feature management and serving |
| Model Registry | MLflow | Model versioning and lifecycle |
| Inference | BentoML | Model serving with batching |
| PINN | PyTorch | Physics-informed neural networks |

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        AI Platform                                │
├──────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
│  │   Prefect   │  │   MLflow    │  │   Feast     │               │
│  │  Training   │  │  Registry   │  │  Features   │               │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘               │
│         │                │                │                       │
│         └────────────────┼────────────────┘                       │
│                          ▼                                        │
│                  ┌─────────────┐                                  │
│                  │   BentoML   │                                  │
│                  │  Inference  │                                  │
│                  └──────┬──────┘                                  │
│                         │                                         │
└─────────────────────────┼─────────────────────────────────────────┘
                          ▼
                   Backend Services
```

## Directory Structure

```
ai-platform/
├── training/           # Prefect training flows
├── inference/          # BentoML serving
├── features/           # Feast feature definitions
├── models/             # Model definitions
├── pinn/               # Physics-informed neural networks
├── notebooks/          # Research notebooks
├── tests/              # Unit and integration tests
└── configs/            # Environment configurations
```

## Quick Start

```bash
# Install dependencies
pip install -e .

# Start feature store
feast apply

# Start MLflow
mlflow server --backend-store-uri postgresql://... --default-artifact-root s3://...

# Start inference service
bentoml serve hafnium_inference:RiskScoringService
```

## Models

### Risk Scoring Model
- Input: Transaction features, customer profile, network signals
- Output: Risk score (0-1), risk level, contributing factors
- Architecture: Gradient boosting ensemble with neural calibration

### Anomaly Detection Model
- Input: Time series of transaction patterns
- Output: Anomaly score, anomaly type classification
- Architecture: Variational autoencoder with attention

### Document Verification Model
- Input: Document image
- Output: Document type, extracted fields, authenticity score
- Architecture: Vision transformer with OCR head

## Training

Training pipelines are orchestrated with Prefect:

```bash
# Run training pipeline
prefect deployment run risk-model-training/production

# Monitor training
prefect server start
```

## Feature Store

Features are managed with Feast:

```bash
# Apply feature definitions
cd features && feast apply

# Materialize features
feast materialize-incremental $(date +%Y-%m-%d)
```

## Inference

Models are served via BentoML:

```bash
# Build bento
bentoml build

# Serve
bentoml serve hafnium_inference:RiskScoringService --port 8001
```

## Monitoring

- Model metrics exported to Prometheus
- Drift detection integrated with Grafana alerts
- Inference telemetry published to Kafka

## License

Proprietary. All rights reserved.
