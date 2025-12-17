# MLOps Architecture

This document describes the Machine Learning Operations architecture for Hafnium.

---

## MLOps Lifecycle

```mermaid
graph LR
    subgraph "Development"
        EXP[Experiment]
        TRAIN[Train]
        EVAL[Evaluate]
    end
    
    subgraph "Registry"
        REG[Model Registry]
        VER[Versioning]
        META[Metadata]
    end
    
    subgraph "Deployment"
        STAGE[Staging]
        CANARY[Canary]
        PROD[Production]
    end
    
    subgraph "Monitoring"
        PERF[Performance]
        DRIFT[Drift Detection]
        ALERT[Alerting]
    end
    
    EXP --> TRAIN --> EVAL
    EVAL --> REG
    REG --> VER & META
    
    REG --> STAGE --> CANARY --> PROD
    
    PROD --> PERF --> DRIFT --> ALERT
    ALERT -->|Retrain| EXP
```

---

## Training Pipeline

```mermaid
graph TB
    subgraph "Data"
        RAW[Raw Data]
        DVC[DVC Versioned]
        FEAT[Feature Store]
    end
    
    subgraph "Training"
        PREP[Preprocessing]
        TRAIN[Model Training]
        VAL[Validation]
    end
    
    subgraph "Artifacts"
        MODEL[Model File]
        META[Metadata]
        CARD[Model Card]
    end
    
    subgraph "Registry"
        MLFLOW[MLflow Registry]
    end
    
    RAW --> DVC --> FEAT
    FEAT --> PREP --> TRAIN --> VAL
    
    VAL -->|Pass| MODEL
    VAL -->|Pass| META
    VAL -->|Pass| CARD
    
    MODEL --> MLFLOW
    META --> MLFLOW
    CARD --> MLFLOW
```

---

## Model Deployment Flow

```mermaid
sequenceDiagram
    participant DS as Data Scientist
    participant REG as Model Registry
    participant CI as CI/CD
    participant STAGE as Staging
    participant CANARY as Canary
    participant PROD as Production
    participant MON as Monitoring
    
    DS->>REG: Register model v2
    DS->>REG: Request promotion
    
    REG->>CI: Trigger deployment
    CI->>STAGE: Deploy to staging
    STAGE->>STAGE: Run tests
    
    alt Tests pass
        STAGE->>CANARY: Deploy 5% traffic
        CANARY->>MON: Monitor metrics
        
        alt Metrics healthy
            CANARY->>PROD: Full rollout
        else Metrics degraded
            CANARY->>PROD: Rollback
        end
    else Tests fail
        STAGE->>REG: Mark rejected
    end
```

---

## Inference Architecture

```mermaid
graph TB
    subgraph "API Layer"
        JAVA[Java Backend]
    end
    
    subgraph "ML Inference"
        LB[Load Balancer]
        INF1[Inference Pod 1]
        INF2[Inference Pod 2]
        INF3[Inference Pod 3]
    end
    
    subgraph "Model Store"
        S3[S3/MinIO]
        CACHE[Redis Cache]
    end
    
    subgraph "Monitoring"
        PROM[Prometheus]
        DRIFT[Drift Detector]
    end
    
    JAVA -->|gRPC/HTTP| LB
    LB --> INF1 & INF2 & INF3
    
    INF1 & INF2 & INF3 --> S3
    INF1 & INF2 & INF3 --> CACHE
    
    INF1 & INF2 & INF3 -->|Metrics| PROM
    INF1 & INF2 & INF3 -->|Predictions| DRIFT
```

---

## Data Versioning

```mermaid
graph LR
    subgraph "Sources"
        TXN[Transactions]
        CUST[Customers]
        EXT[External Data]
    end
    
    subgraph "Processing"
        ETL[ETL Pipeline]
        FE[Feature Engineering]
    end
    
    subgraph "Versioning"
        DVC[DVC]
        GIT[Git LFS]
    end
    
    subgraph "Storage"
        LAKE[Data Lake]
        FS[Feature Store]
    end
    
    TXN & CUST & EXT --> ETL --> FE
    FE --> DVC --> GIT
    FE --> LAKE
    FE --> FS
```

---

## Drift Detection Pipeline

```mermaid
graph TB
    subgraph "Production"
        INF[Inference Service]
        LOG[Prediction Logs]
    end
    
    subgraph "Analysis"
        COLLECT[Collector]
        STAT[Statistical Tests]
        EMBED[Embedding Drift]
    end
    
    subgraph "Detection"
        KS[KS Test]
        PSI[PSI]
        JS[JS Divergence]
    end
    
    subgraph "Response"
        ALERT[Alert]
        RETRAIN[Trigger Retrain]
        ROLLBACK[Rollback]
    end
    
    INF --> LOG --> COLLECT
    COLLECT --> STAT --> KS & PSI
    COLLECT --> EMBED --> JS
    
    KS & PSI & JS -->|Drift Detected| ALERT
    ALERT --> RETRAIN
    ALERT -->|Severe| ROLLBACK
```

---

## Model Governance

| Stage | Review Required | Approvers |
|-------|-----------------|-----------|
| Experiment | No | - |
| Candidate | Yes | Data Scientist Lead |
| Staging | Yes | ML Engineer |
| Production | Yes | Model Risk + Compliance |

---

## Metrics and SLOs

### Inference Service

| Metric | SLO | Measurement |
|--------|-----|-------------|
| Availability | 99.9% | Successful requests / Total |
| Latency P99 | 100ms | histogram_quantile |
| Error Rate | < 0.1% | 5xx / Total |

### Model Quality

| Metric | Threshold | Action |
|--------|-----------|--------|
| PSI | < 0.2 | Alert |
| AUC Drift | < 5% | Retrain |
| Prediction Shift | < 10% | Investigate |

---

## Fallback Strategy

```mermaid
stateDiagram-v2
    [*] --> MLInference
    
    MLInference --> CircuitOpen: 5 failures
    CircuitOpen --> RulesOnly: Fallback
    RulesOnly --> MLInference: Circuit closes
    
    MLInference --> [*]: Success
    RulesOnly --> [*]: Success
```

When ML inference is unavailable:
1. Circuit breaker opens after 5 consecutive failures
2. Traffic routes to rules-only scoring
3. Alert fired to on-call
4. Circuit attempts reset after 30 seconds
