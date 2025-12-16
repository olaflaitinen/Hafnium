-- Risk Engine Service Schema
-- V1__create_risk_schema.sql

-- Risk decisions table (immutable audit trail)
CREATE TABLE risk_decisions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    score DECIMAL(5, 4) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    feature_version VARCHAR(50),
    reasons JSONB NOT NULL DEFAULT '[]',
    policy_actions JSONB NOT NULL DEFAULT '[]',
    context JSONB,
    computed_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        decision_metadata JSONB DEFAULT '{}'
);

CREATE INDEX idx_risk_decisions_tenant_id ON risk_decisions (tenant_id);

CREATE INDEX idx_risk_decisions_entity ON risk_decisions (entity_type, entity_id);

CREATE INDEX idx_risk_decisions_computed_at ON risk_decisions (computed_at);

CREATE INDEX idx_risk_decisions_risk_level ON risk_decisions (risk_level);

-- Reason codes reference table
CREATE TABLE reason_codes (
    code VARCHAR(50) PRIMARY KEY,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

-- Model registry metadata (read-only mirror from MLflow)
CREATE TABLE model_registry (
    model_id VARCHAR(100) PRIMARY KEY,
    model_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    metrics JSONB DEFAULT '{}',
    training_metadata JSONB DEFAULT '{}',
    registered_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL,
        last_synced_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_model_registry_status ON model_registry (status);

-- Model inference telemetry
CREATE TABLE model_inference_telemetry (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    model_id VARCHAR(100) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    latency_ms INTEGER NOT NULL,
    output_distribution JSONB,
    feature_count INTEGER,
    timestamp TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inference_telemetry_model ON model_inference_telemetry (model_id, model_version);

CREATE INDEX idx_inference_telemetry_timestamp ON model_inference_telemetry (timestamp);

-- Insert default reason codes
INSERT INTO
    reason_codes (
        code,
        description,
        category,
        severity
    )
VALUES (
        'HIGH_TXN_VELOCITY',
        'High transaction velocity detected',
        'velocity',
        'high'
    ),
    (
        'UNUSUAL_AMOUNT',
        'Transaction amount significantly differs from historical pattern',
        'amount',
        'medium'
    ),
    (
        'NEW_COUNTERPARTY',
        'Transaction with previously unseen counterparty',
        'network',
        'low'
    ),
    (
        'HIGH_RISK_COUNTRY',
        'Transaction involves high-risk jurisdiction',
        'geographic',
        'high'
    ),
    (
        'STRUCTURING_PATTERN',
        'Pattern consistent with structuring behavior',
        'pattern',
        'critical'
    ),
    (
        'SANCTIONS_MATCH',
        'Potential sanctions list match',
        'screening',
        'critical'
    ),
    (
        'PEP_MATCH',
        'Potential politically exposed person match',
        'screening',
        'high'
    ),
    (
        'DEVICE_ANOMALY',
        'Unusual device or session characteristics',
        'security',
        'medium'
    ),
    (
        'VELOCITY_SPIKE',
        'Sudden increase in transaction frequency',
        'velocity',
        'high'
    ),
    (
        'NETWORK_RISK',
        'Connected to high-risk entities in network',
        'network',
        'high'
    );