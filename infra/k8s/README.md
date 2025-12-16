# Hafnium Kubernetes Infrastructure

This directory contains Kubernetes manifests for deploying the Hafnium platform.

## Directory Structure

```
k8s/
├── base/                    # Base Kustomize overlays
│   ├── kustomization.yaml
│   └── namespace.yaml
├── overlays/
│   ├── development/         # Development environment
│   ├── staging/             # Staging environment
│   └── production/          # Production environment
└── README.md
```

## Prerequisites

- Kubernetes 1.28+
- kubectl configured for target cluster
- Kustomize 5.0+ (or kubectl with kustomize support)
- Secrets management solution (Vault, External Secrets, etc.)

## Deployment

### Development

```bash
kubectl apply -k overlays/development
```

### Staging

```bash
kubectl apply -k overlays/staging
```

### Production

```bash
kubectl apply -k overlays/production
```

## Namespace Conventions

| Namespace | Purpose |
|-----------|---------|
| `hafnium-system` | Core platform services |
| `hafnium-{tenant}` | Tenant-specific workloads |
| `hafnium-observability` | Monitoring and logging |

## Resource Conventions

### Labels

All resources include standard labels:

```yaml
labels:
  app.kubernetes.io/name: {service-name}
  app.kubernetes.io/component: {component}
  app.kubernetes.io/part-of: hafnium
  app.kubernetes.io/managed-by: kustomize
  hafnium.dev/tenant: {tenant-id}
```

### Annotations

Security and compliance annotations:

```yaml
annotations:
  hafnium.dev/data-classification: restricted
  hafnium.dev/compliance-scope: pci-dss,gdpr
```

## Security Notes

- All workloads run as non-root
- Network policies restrict inter-service communication
- Pod Security Standards: restricted
- Secrets injected via External Secrets Operator

---

**DISCLAIMER**: These manifests are provided as reference implementations. Production deployments require security review and customization.
