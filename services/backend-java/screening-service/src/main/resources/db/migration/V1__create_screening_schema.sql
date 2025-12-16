-- Screening Service Schema
-- V1__create_screening_schema.sql

-- Sanctions entities table (lists from OFAC, EU, UN, etc.)
CREATE TABLE sanctions_entities (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    list_source VARCHAR(50) NOT NULL,
    list_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    primary_name VARCHAR(500) NOT NULL,
    name_normalized VARCHAR(500) NOT NULL,
    aliases JSONB DEFAULT '[]',
    identifiers JSONB DEFAULT '[]',
    birth_date DATE,
    countries JSONB DEFAULT '[]',
    programs JSONB DEFAULT '[]',
    remarks TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    added_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sanctions_entities_tenant ON sanctions_entities (tenant_id);

CREATE INDEX idx_sanctions_entities_name ON sanctions_entities (name_normalized);

CREATE INDEX idx_sanctions_entities_list ON sanctions_entities (list_source, list_type);

CREATE INDEX idx_sanctions_entities_active ON sanctions_entities (active);

-- Screening matches table
CREATE TABLE screening_matches (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    request_id UUID NOT NULL,
    entity_id UUID REFERENCES sanctions_entities (id),
    query_name VARCHAR(500) NOT NULL,
    matched_name VARCHAR(500) NOT NULL,
    match_score DECIMAL(5, 4) NOT NULL,
    match_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
    resolved_by UUID,
    resolution VARCHAR(50),
    resolution_notes TEXT,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        resolved_at TIMESTAMP
    WITH
        TIME ZONE
);

CREATE INDEX idx_screening_matches_tenant ON screening_matches (tenant_id);

CREATE INDEX idx_screening_matches_request ON screening_matches (request_id);

CREATE INDEX idx_screening_matches_status ON screening_matches (status);

CREATE INDEX idx_screening_matches_score ON screening_matches (match_score);

-- List import history
CREATE TABLE list_imports (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    list_source VARCHAR(50) NOT NULL,
    file_name VARCHAR(255),
    records_imported INTEGER NOT NULL DEFAULT 0,
    records_updated INTEGER NOT NULL DEFAULT 0,
    records_deleted INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'processing',
    error_message TEXT,
    started_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        completed_at TIMESTAMP
    WITH
        TIME ZONE
);

CREATE INDEX idx_list_imports_tenant ON list_imports (tenant_id);

CREATE INDEX idx_list_imports_source ON list_imports (list_source);