-- Signals Service Schema
-- V1__create_signals_schema.sql

-- Security signals table
CREATE TABLE signals (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    signal_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    source VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(255),
    description TEXT NOT NULL,
    indicators JSONB DEFAULT '[]',
    metadata JSONB DEFAULT '{}',
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        acknowledged_at TIMESTAMP
    WITH
        TIME ZONE,
        acknowledged_by UUID
);

CREATE INDEX idx_signals_tenant ON signals (tenant_id);

CREATE INDEX idx_signals_type ON signals (signal_type);

CREATE INDEX idx_signals_entity ON signals (entity_type, entity_id);

CREATE INDEX idx_signals_status ON signals (status);

CREATE INDEX idx_signals_created ON signals (created_at);

-- Signal correlations
CREATE TABLE signal_correlations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    signal_id UUID NOT NULL REFERENCES signals (id),
    related_signal_id UUID NOT NULL REFERENCES signals (id),
    correlation_type VARCHAR(50) NOT NULL,
    confidence DECIMAL(5, 4),
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_correlations_signal ON signal_correlations (signal_id);

CREATE INDEX idx_correlations_related ON signal_correlations (related_signal_id);