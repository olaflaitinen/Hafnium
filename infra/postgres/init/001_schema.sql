-- Hafnium Database Schema
-- PostgreSQL 16+

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================================================
-- TENANTS
-- =============================================================================

CREATE TABLE tenants (
    tenant_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    config JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_external_id ON tenants (external_id);

CREATE INDEX idx_tenants_status ON tenants (status);

-- =============================================================================
-- CUSTOMERS
-- =============================================================================

CREATE TABLE customers (
    customer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    external_id VARCHAR(255) NOT NULL,
    customer_type VARCHAR(50) NOT NULL DEFAULT 'individual',
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    risk_tier VARCHAR(50),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, external_id)
);

CREATE INDEX idx_customers_tenant ON customers (tenant_id);

CREATE INDEX idx_customers_status ON customers (status);

CREATE INDEX idx_customers_risk_tier ON customers (risk_tier);

CREATE INDEX idx_customers_created_at ON customers (created_at);

-- =============================================================================
-- KYC WORKFLOWS
-- =============================================================================

CREATE TABLE kyc_workflows (
    workflow_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID NOT NULL REFERENCES customers (customer_id),
    workflow_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'initiated',
    current_step VARCHAR(100),
    steps JSONB DEFAULT '[]',
    result JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_kyc_workflows_tenant ON kyc_workflows (tenant_id);

CREATE INDEX idx_kyc_workflows_customer ON kyc_workflows (customer_id);

CREATE INDEX idx_kyc_workflows_status ON kyc_workflows (status);

-- =============================================================================
-- DOCUMENTS
-- =============================================================================

CREATE TABLE documents (
    document_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID NOT NULL REFERENCES customers (customer_id),
    workflow_id UUID REFERENCES kyc_workflows (workflow_id),
    document_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'uploaded',
    storage_ref VARCHAR(500) NOT NULL,
    extracted_data JSONB,
    verification_result JSONB,
    fraud_score DECIMAL(5, 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_tenant ON documents (tenant_id);

CREATE INDEX idx_documents_customer ON documents (customer_id);

CREATE INDEX idx_documents_workflow ON documents (workflow_id);

CREATE INDEX idx_documents_status ON documents (status);

-- =============================================================================
-- TRANSACTIONS
-- =============================================================================

CREATE TABLE transactions (
    txn_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID REFERENCES customers (customer_id),
    external_txn_id VARCHAR(255),
    amount DECIMAL(20, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    txn_type VARCHAR(50) NOT NULL,
    txn_timestamp TIMESTAMPTZ NOT NULL,
    counterparty_id VARCHAR(255),
    counterparty_name VARCHAR(500),
    channel VARCHAR(50),
    geo_data JSONB,
    metadata JSONB DEFAULT '{}',
    risk_score DECIMAL(5, 4),
    risk_factors JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
)
PARTITION BY
    RANGE (txn_timestamp);

CREATE INDEX idx_transactions_tenant ON transactions (tenant_id);

CREATE INDEX idx_transactions_customer ON transactions (customer_id);

CREATE INDEX idx_transactions_timestamp ON transactions (txn_timestamp);

CREATE INDEX idx_transactions_risk_score ON transactions (risk_score);

-- Create monthly partitions for transactions (example: 2025)
CREATE TABLE transactions_2025_01 PARTITION OF transactions FOR
VALUES
FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE transactions_2025_02 PARTITION OF transactions FOR
VALUES
FROM ('2025-02-01') TO ('2025-03-01');

CREATE TABLE transactions_2025_03 PARTITION OF transactions FOR
VALUES
FROM ('2025-03-01') TO ('2025-04-01');

CREATE TABLE transactions_2025_04 PARTITION OF transactions FOR
VALUES
FROM ('2025-04-01') TO ('2025-05-01');

CREATE TABLE transactions_2025_05 PARTITION OF transactions FOR
VALUES
FROM ('2025-05-01') TO ('2025-06-01');

CREATE TABLE transactions_2025_06 PARTITION OF transactions FOR
VALUES
FROM ('2025-06-01') TO ('2025-07-01');

CREATE TABLE transactions_2025_07 PARTITION OF transactions FOR
VALUES
FROM ('2025-07-01') TO ('2025-08-01');

CREATE TABLE transactions_2025_08 PARTITION OF transactions FOR
VALUES
FROM ('2025-08-01') TO ('2025-09-01');

CREATE TABLE transactions_2025_09 PARTITION OF transactions FOR
VALUES
FROM ('2025-09-01') TO ('2025-10-01');

CREATE TABLE transactions_2025_10 PARTITION OF transactions FOR
VALUES
FROM ('2025-10-01') TO ('2025-11-01');

CREATE TABLE transactions_2025_11 PARTITION OF transactions FOR
VALUES
FROM ('2025-11-01') TO ('2025-12-01');

CREATE TABLE transactions_2025_12 PARTITION OF transactions FOR
VALUES
FROM ('2025-12-01') TO ('2026-01-01');

-- =============================================================================
-- SCREENING
-- =============================================================================

CREATE TABLE screening_requests (
    request_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    query_data JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    result JSONB,
    match_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_screening_tenant ON screening_requests (tenant_id);

CREATE INDEX idx_screening_entity ON screening_requests (entity_type, entity_id);

CREATE INDEX idx_screening_status ON screening_requests (status);

CREATE TABLE screening_matches (
    match_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    request_id UUID NOT NULL REFERENCES screening_requests (request_id),
    list_name VARCHAR(255) NOT NULL,
    match_score DECIMAL(5, 4) NOT NULL,
    matched_name VARCHAR(500),
    matched_data JSONB,
    disposition VARCHAR(50) DEFAULT 'pending',
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_screening_matches_request ON screening_matches (request_id);

CREATE INDEX idx_screening_matches_disposition ON screening_matches (disposition);

-- =============================================================================
-- ALERTS
-- =============================================================================

CREATE TABLE alerts (
    alert_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID REFERENCES customers (customer_id),
    txn_id UUID,
    rule_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    score DECIMAL(5, 4),
    status VARCHAR(50) NOT NULL DEFAULT 'new',
    explanation TEXT,
    triggered_conditions JSONB,
    case_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMPTZ,
    acknowledged_by UUID
);

CREATE INDEX idx_alerts_tenant ON alerts (tenant_id);

CREATE INDEX idx_alerts_customer ON alerts (customer_id);

CREATE INDEX idx_alerts_status ON alerts (status);

CREATE INDEX idx_alerts_severity ON alerts (severity);

CREATE INDEX idx_alerts_case ON alerts (case_id);

CREATE INDEX idx_alerts_created_at ON alerts (created_at);

-- =============================================================================
-- CASES
-- =============================================================================

CREATE TABLE cases (
    case_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID REFERENCES customers (customer_id),
    case_number VARCHAR(50) NOT NULL,
    case_type VARCHAR(100) NOT NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'medium',
    status VARCHAR(50) NOT NULL DEFAULT 'open',
    assigned_to UUID,
    summary TEXT,
    resolution VARCHAR(100),
    disposition VARCHAR(100),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    UNIQUE (tenant_id, case_number)
);

CREATE INDEX idx_cases_tenant ON cases (tenant_id);

CREATE INDEX idx_cases_customer ON cases (customer_id);

CREATE INDEX idx_cases_status ON cases (status);

CREATE INDEX idx_cases_priority ON cases (priority);

CREATE INDEX idx_cases_assigned ON cases (assigned_to);

CREATE TABLE case_notes (
    note_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    case_id UUID NOT NULL REFERENCES cases (case_id),
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'note',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_notes_case ON case_notes (case_id);

CREATE TABLE case_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    case_id UUID NOT NULL REFERENCES cases (case_id),
    evidence_type VARCHAR(100) NOT NULL,
    storage_ref VARCHAR(500),
    description TEXT,
    metadata JSONB DEFAULT '{}',
    added_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_evidence_case ON case_evidence (case_id);

-- =============================================================================
-- RISK SCORES
-- =============================================================================

CREATE TABLE risk_scores (
    score_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    score DECIMAL(5, 4) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    model_version VARCHAR(100) NOT NULL,
    reasons JSONB,
    features JSONB,
    policy_actions JSONB,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_risk_scores_entity ON risk_scores (entity_type, entity_id);

CREATE INDEX idx_risk_scores_computed ON risk_scores (computed_at);

CREATE UNIQUE INDEX idx_risk_scores_latest ON risk_scores (
    entity_type,
    entity_id,
    computed_at DESC
);

-- =============================================================================
-- AUDIT LOG
-- =============================================================================

CREATE TABLE audit_log (
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

CREATE INDEX idx_audit_log_tenant ON audit_log (tenant_id);

CREATE INDEX idx_audit_log_actor ON audit_log (actor_id);

CREATE INDEX idx_audit_log_resource ON audit_log (resource_type, resource_id);

CREATE INDEX idx_audit_log_created ON audit_log (created_at);

-- =============================================================================
-- TOKENS (Tokenization)
-- =============================================================================

CREATE TABLE tokens (
    token_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    token VARCHAR(64) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    encrypted_value BYTEA NOT NULL,
    key_version INTEGER NOT NULL DEFAULT 1,
    purpose VARCHAR(100),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_accessed_at TIMESTAMPTZ
);

CREATE INDEX idx_tokens_tenant ON tokens (tenant_id);

CREATE INDEX idx_tokens_token ON tokens (token);

CREATE INDEX idx_tokens_expires ON tokens (expires_at);

-- =============================================================================
-- SESSIONS (Shield)
-- =============================================================================

CREATE TABLE sessions (
    session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    tenant_id UUID NOT NULL REFERENCES tenants (tenant_id),
    customer_id UUID REFERENCES customers (customer_id),
    device_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    geo_data JSONB,
    risk_score DECIMAL(5, 4),
    risk_signals JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ
);

CREATE INDEX idx_sessions_tenant ON sessions (tenant_id);

CREATE INDEX idx_sessions_customer ON sessions (customer_id);

CREATE INDEX idx_sessions_device ON sessions (device_id);

CREATE INDEX idx_sessions_status ON sessions (status);

-- =============================================================================
-- SAGA STATE
-- =============================================================================

CREATE TABLE saga_state (
    saga_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
    saga_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    current_step INTEGER NOT NULL DEFAULT 0,
    context JSONB NOT NULL DEFAULT '{}',
    completed_steps JSONB DEFAULT '[]',
    error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_saga_state_type ON saga_state (saga_type);

CREATE INDEX idx_saga_state_status ON saga_state (status);

-- =============================================================================
-- FUNCTIONS
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_kyc_workflows_updated_at BEFORE UPDATE ON kyc_workflows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cases_updated_at BEFORE UPDATE ON cases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_saga_state_updated_at BEFORE UPDATE ON saga_state
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- ROW LEVEL SECURITY
-- =============================================================================

-- Enable RLS on tenant-scoped tables
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

-- RLS policies will be created based on application configuration
-- Example policy (to be customized):
-- CREATE POLICY tenant_isolation ON customers
--     USING (tenant_id = current_setting('app.current_tenant_id')::UUID);