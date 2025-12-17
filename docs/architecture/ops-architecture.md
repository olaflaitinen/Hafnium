# Ops Architecture

This document describes the operational architecture of the Hafnium platform.

---

## Deployment Architecture

```mermaid
graph TB
    subgraph "Edge Layer"
        LB[Load Balancer]
        CDN[CDN]
    end
    
    subgraph "API Layer"
        APIGW[API Gateway<br/>Envoy]
    end
    
    subgraph "Application Layer"
        AF[api-facade]
        IS[identity-service]
        SS[screening-service]
        MS[monitoring-service]
        CS[case-service]
        VS[vault-service]
        RS[risk-engine-service]
        SG[signals-service]
    end
    
    subgraph "Data Layer"
        PG[(PostgreSQL)]
        RD[(Redis)]
        KF[Kafka/Redpanda]
        MN[MinIO]
    end
    
    subgraph "Security Layer"
        KC[Keycloak]
        OPA[OPA]
        VT[Vault]
    end
    
    subgraph "ML Layer"
        AI[AI Inference]
        SP[Stream Processor]
    end
    
    LB --> APIGW
    CDN --> LB
    APIGW --> AF
    
    AF --> IS & SS & MS & CS & VS & RS & SG
    
    IS & SS & MS & CS & VS & RS & SG --> PG
    IS & SS & MS & CS & RS & SG --> KF
    IS & SS & MS & CS & VS & RS & SG --> RD
    CS --> MN
    
    IS & SS & MS & CS & VS & RS & SG --> KC
    IS & SS & MS & CS & VS & RS & SG --> OPA
    VS --> VT
    
    RS --> AI
    MS --> SP
```

---

## CI/CD Pipeline

```mermaid
graph LR
    subgraph "Source"
        GH[GitHub]
    end
    
    subgraph "CI"
        B[Build]
        T[Test]
        L[Lint]
        S[SAST]
        D[Dependency Scan]
    end
    
    subgraph "Security"
        CQ[CodeQL]
        TR[Trivy]
        SB[SBOM]
        PR[Provenance]
        SG[Signing]
    end
    
    subgraph "Registry"
        GHCR[GHCR]
        REL[Releases]
    end
    
    subgraph "Deploy"
        ARGO[ArgoCD]
        K8S[Kubernetes]
    end
    
    GH --> B --> T --> L
    B --> S --> D
    S --> CQ
    D --> TR
    
    T --> SB --> PR --> SG
    SG --> GHCR
    SG --> REL
    
    GHCR --> ARGO --> K8S
```

---

## Kubernetes Architecture

```mermaid
graph TB
    subgraph "Cluster"
        subgraph "hafnium namespace"
            subgraph "Backend Services"
                IS[identity-service<br/>2 replicas]
                SS[screening-service<br/>2 replicas]
                MS[monitoring-service<br/>2 replicas]
                CS[case-service<br/>2 replicas]
            end
            
            subgraph "Platform Services"
                VS[vault-service]
                RS[risk-engine-service]
                SG[signals-service]
                AF[api-facade]
            end
        end
        
        subgraph "data namespace"
            PG[PostgreSQL<br/>HA Cluster]
            RD[Redis<br/>Sentinel]
            KF[Kafka<br/>3 brokers]
        end
        
        subgraph "security namespace"
            KC[Keycloak]
            OPA[OPA]
        end
        
        subgraph "observability namespace"
            PROM[Prometheus]
            GRAF[Grafana]
            TEMPO[Tempo]
            LOKI[Loki]
        end
    end
    
    IS & SS & MS & CS --> PG
    IS & SS & MS & CS --> RD
    IS & SS & MS & CS --> KF
```

---

## GitOps Flow

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant GH as GitHub
    participant CI as GitHub Actions
    participant REG as Container Registry
    participant ARGO as ArgoCD
    participant K8S as Kubernetes
    
    Dev->>GH: Push code
    GH->>CI: Trigger workflow
    CI->>CI: Build and test
    CI->>CI: Security scans
    CI->>REG: Push image
    CI->>GH: Update manifests
    
    ARGO->>GH: Poll for changes
    ARGO->>REG: Pull image
    ARGO->>K8S: Apply manifests
    K8S->>K8S: Rolling update
```

---

## Observability Stack

```mermaid
graph LR
    subgraph "Sources"
        APP[Applications]
        K8S[Kubernetes]
        INF[Infrastructure]
    end
    
    subgraph "Collection"
        OTEL[OpenTelemetry Collector]
    end
    
    subgraph "Storage"
        PROM[Prometheus]
        LOKI[Loki]
        TEMPO[Tempo]
    end
    
    subgraph "Visualization"
        GRAF[Grafana]
    end
    
    subgraph "Alerting"
        AM[Alertmanager]
        PD[PagerDuty]
    end
    
    APP -->|Metrics| OTEL
    APP -->|Logs| OTEL
    APP -->|Traces| OTEL
    
    K8S -->|Metrics| OTEL
    INF -->|Metrics| OTEL
    
    OTEL -->|Metrics| PROM
    OTEL -->|Logs| LOKI
    OTEL -->|Traces| TEMPO
    
    PROM --> GRAF
    LOKI --> GRAF
    TEMPO --> GRAF
    
    PROM --> AM --> PD
```

---

## Disaster Recovery

| Component | RPO | RTO | Strategy |
|-----------|-----|-----|----------|
| Database | 1 hour | 4 hours | Continuous backup + PITR |
| Kafka | 0 | 30 min | Multi-broker replication |
| MinIO | 24 hours | 4 hours | Cross-region replication |
| Secrets | 0 | 1 hour | Vault HA + backup |

---

## Network Topology

```mermaid
graph TB
    subgraph "Internet"
        USER[Users]
    end
    
    subgraph "Edge"
        WAF[WAF]
        LB[Load Balancer]
    end
    
    subgraph "DMZ"
        APIGW[API Gateway]
    end
    
    subgraph "Application Zone"
        SVC[Backend Services]
    end
    
    subgraph "Data Zone"
        DB[(Databases)]
    end
    
    subgraph "Management Zone"
        MON[Monitoring]
        LOG[Logging]
    end
    
    USER -->|HTTPS| WAF --> LB --> APIGW
    APIGW -->|mTLS| SVC
    SVC -->|Encrypted| DB
    SVC -->|Push| MON
    SVC -->|Push| LOG
```
