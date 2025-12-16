-- Identity Service Schema
-- V1__create_identity_schema.sql

-- Tenants table (shared across services but managed here)
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_status ON tenants (status);

-- Customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    external_id VARCHAR(255) NOT NULL,
    customer_type VARCHAR(50) NOT NULL DEFAULT 'individual',
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    risk_tier VARCHAR(20),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        deleted_at TIMESTAMP
    WITH
        TIME ZONE,
        UNIQUE (tenant_id, external_id)
);

CREATE INDEX idx_customers_tenant_id ON customers (tenant_id);

CREATE INDEX idx_customers_status ON customers (status);

CREATE INDEX idx_customers_external_id ON customers (tenant_id, external_id);

-- KYC Workflows table
CREATE TABLE kyc_workflows (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers (id),
    workflow_type VARCHAR(50) NOT NULL DEFAULT 'standard',
    status VARCHAR(50) NOT NULL DEFAULT 'initiated',
    current_step VARCHAR(100),
    steps JSONB DEFAULT '[]',
    result JSONB,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        completed_at TIMESTAMP
    WITH
        TIME ZONE
);

CREATE INDEX idx_kyc_workflows_tenant_id ON kyc_workflows (tenant_id);

CREATE INDEX idx_kyc_workflows_customer_id ON kyc_workflows (customer_id);

CREATE INDEX idx_kyc_workflows_status ON kyc_workflows (status);

-- Documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers (id),
    workflow_id UUID REFERENCES kyc_workflows (id),
    document_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'uploaded',
    storage_path VARCHAR(512),
    extracted_data JSONB,
    verification_result JSONB,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_tenant_id ON documents (tenant_id);

CREATE INDEX idx_documents_customer_id ON documents (customer_id);

CREATE INDEX idx_documents_workflow_id ON documents (workflow_id);

-- Audit events table (append-only)
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    actor_id UUID,
    action VARCHAR(50) NOT NULL,
    payload_hash VARCHAR(64),
    metadata JSONB DEFAULT '{}',
    occurred_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_tenant_id ON audit_events (tenant_id);

CREATE INDEX idx_audit_events_entity ON audit_events (entity_type, entity_id);

CREATE INDEX idx_audit_events_occurred_at ON audit_events (occurred_at);

-- Ensure audit table is append-only via trigger
CREATE OR REPLACE FUNCTION prevent_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit events cannot be modified or deleted';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_events_immutable
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_modification();