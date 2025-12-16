-- Vault Service Schema
-- V1__create_vault_schema.sql

-- Token mappings (sensitive data tokens)
CREATE TABLE tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    token VARCHAR(64) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    encrypted_value BYTEA NOT NULL,
    iv BYTEA NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        last_accessed_at TIMESTAMP
    WITH
        TIME ZONE,
        access_count INTEGER NOT NULL DEFAULT 0,
        UNIQUE (tenant_id, token)
);

CREATE INDEX idx_tokens_tenant ON tokens (tenant_id);

CREATE INDEX idx_tokens_token ON tokens (tenant_id, token);

CREATE INDEX idx_tokens_data_type ON tokens (data_type);

-- Access log (immutable)
CREATE TABLE access_log (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    token_id UUID REFERENCES tokens (id),
    operation VARCHAR(20) NOT NULL,
    actor_id UUID NOT NULL,
    reason TEXT,
    ip_address INET,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    occurred_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_access_log_tenant ON access_log (tenant_id);

CREATE INDEX idx_access_log_token ON access_log (token_id);

CREATE INDEX idx_access_log_actor ON access_log (actor_id);

CREATE INDEX idx_access_log_occurred ON access_log (occurred_at);

-- Prevent modification of access log
CREATE OR REPLACE FUNCTION prevent_access_log_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Access log cannot be modified or deleted';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER access_log_immutable
    BEFORE UPDATE OR DELETE ON access_log
    FOR EACH ROW EXECUTE FUNCTION prevent_access_log_modification();