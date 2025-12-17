# Backend Deployment Guide

## Overview

This guide covers deployment of the Hafnium backend services to various environments.

## Prerequisites

- Kubernetes 1.28+
- Helm 3.12+
- PostgreSQL 15+
- Apache Kafka (or Redpanda)
- Keycloak 22+

## Local Development

### Using Docker Compose

```bash
# Start all infrastructure
cd /path/to/hafnium
docker compose up -d

# Build and run services
cd services/backend-java
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Individual Services

```bash
# Run specific service
./gradlew :identity-service:bootRun

# With custom port
./gradlew :screening-service:bootRun --args='--server.port=8082'
```

## Kubernetes Deployment

### Using Helm

```bash
# Add namespace
kubectl create namespace hafnium

# Install with Helm
helm install hafnium-backend ./infra/helm/hafnium-backend \
  --namespace hafnium \
  -f ./infra/helm/hafnium-backend/values.yaml

# Upgrade
helm upgrade hafnium-backend ./infra/helm/hafnium-backend \
  --namespace hafnium \
  --set common.replicaCount=3
```

### Environment Overlays

```bash
# Development
helm install hafnium-backend ./infra/helm/hafnium-backend \
  -f ./infra/helm/hafnium-backend/values.yaml \
  -f ./infra/helm/hafnium-backend/values-dev.yaml

# Production
helm install hafnium-backend ./infra/helm/hafnium-backend \
  -f ./infra/helm/hafnium-backend/values.yaml \
  -f ./infra/helm/hafnium-backend/values-prod.yaml
```

## Using Terraform

```bash
cd infra/terraform

# Initialize
terraform init

# Plan
terraform plan -var-file=environments/dev.tfvars.example

# Apply
terraform apply -var-file=environments/dev.tfvars.example
```

## Configuration

### Required Secrets

```bash
# Create database secret
kubectl create secret generic hafnium-db-credentials \
  --from-literal=jdbc-url="jdbc:postgresql://postgres:5432/hafnium" \
  --from-literal=username="hafnium" \
  --from-literal=password="your-password" \
  -n hafnium
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hafnium-config
data:
  SPRING_PROFILES_ACTIVE: "kubernetes"
  SPRING_KAFKA_BOOTSTRAP_SERVERS: "redpanda:9092"
  KEYCLOAK_AUTH_SERVER_URL: "http://keycloak:8080"
  OPA_URL: "http://opa:8181"
```

## Database Migrations

```bash
# Run migrations before deployment
./gradlew :identity-service:flywayMigrate \
  -Dflyway.url=jdbc:postgresql://db-host:5432/hafnium \
  -Dflyway.user=hafnium \
  -Dflyway.password=password
```

## Health Checks

### Liveness Probe

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
```

### Readiness Probe

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

## Scaling

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### Manual Scaling

```bash
kubectl scale deployment identity-service --replicas=5 -n hafnium
```

## Rollback

```bash
# View history
kubectl rollout history deployment/identity-service -n hafnium

# Rollback
kubectl rollout undo deployment/identity-service -n hafnium
```

## Monitoring

### Prometheus Endpoint

All services expose metrics at `/actuator/prometheus`.

### Grafana Dashboards

Import dashboards from `infra/grafana/dashboards/`.

## Troubleshooting

### Pod Won't Start

```bash
kubectl describe pod -n hafnium <pod-name>
kubectl logs -n hafnium <pod-name> --previous
```

### Database Connection

```bash
kubectl exec -it -n hafnium <pod-name> -- \
  java -jar app.jar --spring.profiles.active=debug
```

### Network Issues

```bash
kubectl exec -it -n hafnium <pod-name> -- \
  wget -q -O - http://keycloak:8080/health
```
