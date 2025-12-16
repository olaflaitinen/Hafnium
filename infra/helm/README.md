# Hafnium Helm Charts

This directory contains Helm charts for deploying the Hafnium platform.

## Charts

| Chart | Description |
|-------|-------------|
| `hafnium` | Umbrella chart for full platform deployment |
| `risk-engine` | Risk scoring service |
| `ai-platform` | AI/ML inference service |
| `backend` | Java backend service |
| `frontend` | React frontend |

## Prerequisites

- Helm 3.12+
- Kubernetes 1.28+
- PV provisioner (for persistent storage)

## Quick Start

### Add Repository

```bash
helm repo add hafnium https://charts.hafnium.dev
helm repo update
```

### Install

```bash
helm install hafnium hafnium/hafnium \
  --namespace hafnium-system \
  --create-namespace \
  --values values-production.yaml
```

### Upgrade

```bash
helm upgrade hafnium hafnium/hafnium \
  --namespace hafnium-system \
  --values values-production.yaml
```

## Configuration

See `values.yaml` in each chart for available configuration options.

### Common Values

```yaml
global:
  # Image registry
  imageRegistry: ghcr.io/hafnium
  # Image pull secrets
  imagePullSecrets: []
  # Storage class
  storageClass: standard

# Enable/disable components
components:
  riskEngine:
    enabled: true
  aiPlatform:
    enabled: true
  backend:
    enabled: true
  frontend:
    enabled: true
```

## Development

### Lint Charts

```bash
helm lint charts/*
```

### Template Locally

```bash
helm template hafnium charts/hafnium -f values-dev.yaml
```

### Package

```bash
helm package charts/*
```

---

**DISCLAIMER**: Helm charts are provided as reference implementations. Production deployments require security review.
