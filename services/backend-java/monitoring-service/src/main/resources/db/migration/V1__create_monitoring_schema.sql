-- Monitoring Service Schema
-- V1__create_monitoring_schema.sql

-- Transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    amount DECIMAL(18, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    counterparty_name VARCHAR(255),
    counterparty_account VARCHAR(100),
    counterparty_country VARCHAR(3),
    channel VARCHAR(50),
    reference TEXT,
    metadata JSONB DEFAULT '{}',
    risk_score DECIMAL(5, 4),
    transaction_timestamp TIMESTAMP
    WITH
        TIME ZONE NOT NULL,
        created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_tenant ON transactions (tenant_id);

CREATE INDEX idx_transactions_customer ON transactions (customer_id);

CREATE INDEX idx_transactions_timestamp ON transactions (transaction_timestamp);

CREATE INDEX idx_transactions_risk ON transactions (risk_score);

CREATE INDEX idx_transactions_external ON transactions (tenant_id, external_id);

-- Alerts table
CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    transaction_id UUID REFERENCES transactions (id),
    customer_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'open',
    rule_id VARCHAR(100),
    score DECIMAL(5, 4) NOT NULL,
    reasons JSONB NOT NULL DEFAULT '[]',
    assigned_to UUID,
    case_id UUID,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        resolved_at TIMESTAMP
    WITH
        TIME ZONE
);

CREATE INDEX idx_alerts_tenant ON alerts (tenant_id);

CREATE INDEX idx_alerts_customer ON alerts (customer_id);

CREATE INDEX idx_alerts_status ON alerts (status);

CREATE INDEX idx_alerts_severity ON alerts (severity);

CREATE INDEX idx_alerts_created ON alerts (created_at);

-- Detection rules table
CREATE TABLE detection_rules (
    id VARCHAR(100) PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 100,
    conditions JSONB NOT NULL,
    actions JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_detection_rules_tenant ON detection_rules (tenant_id);

CREATE INDEX idx_detection_rules_enabled ON detection_rules (enabled);

-- Insert default rules
INSERT INTO
    detection_rules (
        id,
        tenant_id,
        name,
        description,
        rule_type,
        conditions,
        actions,
        priority
    )
VALUES (
        'RULE_HIGH_VALUE',
        '00000000-0000-0000-0000-000000000000',
        'High Value Transaction',
        'Alert on transactions above threshold',
        'THRESHOLD',
        '{"field": "amount", "operator": "gt", "value": 10000}',
        '[{"type": "ALERT", "severity": "HIGH"}]',
        100
    ),
    (
        'RULE_VELOCITY',
        '00000000-0000-0000-0000-000000000000',
        'Transaction Velocity',
        'Alert on high transaction frequency',
        'VELOCITY',
        '{"count": 10, "window_hours": 24}',
        '[{"type": "ALERT", "severity": "HIGH"}]',
        90
    ),
    (
        'RULE_HIGH_RISK_COUNTRY',
        '00000000-0000-0000-0000-000000000000',
        'High Risk Country',
        'Alert on transactions to/from high-risk countries',
        'COUNTRY_CHECK',
        '{"countries": ["IR", "KP", "SY", "CU"]}',
        '[{"type": "ALERT", "severity": "CRITICAL"}]',
        80
    );