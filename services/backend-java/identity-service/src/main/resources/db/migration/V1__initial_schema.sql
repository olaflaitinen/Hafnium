-- Hafnium Backend Database Schema
-- Flyway Baseline Migration V1__initial_schema.sql
-- This establishes the baseline schema for all backend services

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tenants table
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    external_id VARCHAR(255) NOT NULL,
    customer_type VARCHAR(50) NOT NULL DEFAULT 'INDIVIDUAL',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    risk_tier VARCHAR(50),
    metadata JSONB DEFAULT '{}',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, external_id)
);

CREATE INDEX idx_customers_tenant ON customers (tenant_id);

CREATE INDEX idx_customers_status ON customers (tenant_id, status);

-- KYC Workflows table
CREATE TABLE IF NOT EXISTS kyc_workflows (
    workflow_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID NOT NULL REFERENCES customers (customer_id),
    workflow_type VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    current_step VARCHAR(100),
    steps JSONB DEFAULT '[]',
    result JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_kyc_workflows_tenant ON kyc_workflows (tenant_id);

CREATE INDEX idx_kyc_workflows_customer ON kyc_workflows (tenant_id, customer_id);

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID NOT NULL REFERENCES customers (customer_id),
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    object_key VARCHAR(500),
    verification_status VARCHAR(50) DEFAULT 'pending',
    verification_result JSONB,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_customer ON documents (tenant_id, customer_id);

-- Transactions table (partitioned by month)
CREATE TABLE IF NOT EXISTS transactions (
    txn_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL,
    customer_id UUID,
    external_txn_id VARCHAR(255),
    amount DECIMAL(20, 4) NOT NULL,
    currency CHAR(3) NOT NULL,
    txn_type VARCHAR(50) NOT NULL,
    txn_timestamp TIMESTAMPTZ NOT NULL,
    counterparty_id VARCHAR(255),
    counterparty_name VARCHAR(255),
    channel VARCHAR(50),
    geo_data JSONB,
    metadata JSONB DEFAULT '{}',
    risk_score DECIMAL(5, 4),
    risk_factors JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_txn_tenant ON transactions (tenant_id);

CREATE INDEX idx_txn_customer ON transactions (tenant_id, customer_id);

CREATE INDEX idx_txn_timestamp ON transactions (tenant_id, txn_timestamp);

-- Screening Requests table
CREATE TABLE IF NOT EXISTS screening_requests (
    request_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    query_data JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    result JSONB,
    match_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_screening_tenant ON screening_requests (tenant_id);

-- Screening Matches table
CREATE TABLE IF NOT EXISTS screening_matches (
    match_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    request_id UUID NOT NULL REFERENCES screening_requests (request_id),
    list_name VARCHAR(100) NOT NULL,
    match_score DECIMAL(5, 4) NOT NULL,
    matched_name VARCHAR(500),
    matched_data JSONB,
    disposition VARCHAR(50) DEFAULT 'PENDING',
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_matches_request ON screening_matches (request_id);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    alert_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID,
    txn_id UUID,
    rule_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    score DECIMAL(5, 4),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    explanation TEXT,
    triggered_conditions JSONB,
    case_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMPTZ,
    acknowledged_by UUID
);

CREATE INDEX idx_alerts_tenant ON alerts (tenant_id);

CREATE INDEX idx_alerts_status ON alerts (tenant_id, status);

CREATE INDEX idx_alerts_severity ON alerts (tenant_id, severity);

-- Cases table
CREATE TABLE IF NOT EXISTS cases (
    case_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    case_type VARCHAR(100) NOT NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    assigned_to UUID,
    customer_id UUID,
    alert_ids JSONB DEFAULT '[]',
    metadata JSONB DEFAULT '{}',
    ai_summary TEXT,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    due_date TIMESTAMPTZ
);

CREATE INDEX idx_cases_tenant ON cases (tenant_id);

CREATE INDEX idx_cases_status ON cases (tenant_id, status);

CREATE INDEX idx_cases_assigned ON cases (tenant_id, assigned_to);

-- Case Notes table
CREATE TABLE IF NOT EXISTS case_notes (
    note_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    case_id UUID NOT NULL REFERENCES cases (case_id),
    content TEXT NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_notes ON case_notes (case_id);

-- Case Evidence table
CREATE TABLE IF NOT EXISTS case_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    case_id UUID NOT NULL REFERENCES cases (case_id),
    evidence_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    object_key VARCHAR(500),
    metadata JSONB DEFAULT '{}',
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_evidence ON case_evidence (case_id);

-- Risk Scores table
CREATE TABLE IF NOT EXISTS risk_scores (
    score_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    score DECIMAL(5, 4) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    factors JSONB DEFAULT '[]',
    model_id VARCHAR(100),
    model_version VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_risk_scores_entity ON risk_scores (
    tenant_id,
    entity_type,
    entity_id
);

-- Tokens table (for vault service)
CREATE TABLE IF NOT EXISTS tokens (
    token_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    token_value VARCHAR(100) NOT NULL UNIQUE,
    data_type VARCHAR(100) NOT NULL,
    encrypted_value BYTEA NOT NULL,
    retention_days INTEGER DEFAULT 365,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_tokens_tenant ON tokens (tenant_id);

CREATE INDEX idx_tokens_value ON tokens (token_value);

-- Sessions table (for signals service)
CREATE TABLE IF NOT EXISTS sessions (
    session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    user_id VARCHAR(255) NOT NULL,
    device_fingerprint VARCHAR(500),
    ip_address INET,
    user_agent TEXT,
    geo_data JSONB,
    risk_score DECIMAL(5, 4),
    step_up_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sessions_tenant ON sessions (tenant_id);

CREATE INDEX idx_sessions_user ON sessions (tenant_id, user_id);

-- Audit Log table (append-only)
CREATE TABLE IF NOT EXISTS audit_log (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    details JSONB,
    previous_hash VARCHAR(64),
    entry_hash VARCHAR(64) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tenant ON audit_log (tenant_id);

CREATE INDEX idx_audit_actor ON audit_log (tenant_id, actor_id);

CREATE INDEX idx_audit_resource ON audit_log (
    tenant_id,
    resource_type,
    resource_id
);

CREATE INDEX idx_audit_created ON audit_log (tenant_id, created_at);

-- Updated at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_kyc_workflows_updated_at BEFORE UPDATE ON kyc_workflows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cases_updated_at BEFORE UPDATE ON cases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Row Level Security (enable for multi-tenant isolation)
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;

ALTER TABLE kyc_workflows ENABLE ROW LEVEL SECURITY;

ALTER TABLE documents ENABLE ROW LEVEL SECURITY;

ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

ALTER TABLE screening_requests ENABLE ROW LEVEL SECURITY;

ALTER TABLE alerts ENABLE ROW LEVEL SECURITY;

ALTER TABLE cases ENABLE ROW LEVEL SECURITY;

ALTER TABLE risk_scores ENABLE ROW LEVEL SECURITY;

ALTER TABLE tokens ENABLE ROW LEVEL SECURITY;

ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;

ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;

-- Insert default tenant for development
INSERT INTO
    tenants (tenant_id, name, status)
VALUES (
        '00000000-0000-0000-0000-000000000001',
        'Default Tenant',
        'active'
    ) ON CONFLICT (name) DO NOTHING;