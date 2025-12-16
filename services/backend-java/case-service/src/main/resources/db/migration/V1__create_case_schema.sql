-- Case Service Schema
-- V1__create_case_schema.sql

-- Cases table
CREATE TABLE cases (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    case_number VARCHAR(50) NOT NULL,
    case_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'open',
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    subject VARCHAR(500) NOT NULL,
    description TEXT,
    customer_id UUID,
    assigned_to UUID,
    team_id UUID,
    alert_ids JSONB DEFAULT '[]',
    tags JSONB DEFAULT '[]',
    sla_due_at TIMESTAMP
    WITH
        TIME ZONE,
        created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        closed_at TIMESTAMP
    WITH
        TIME ZONE,
        UNIQUE (tenant_id, case_number)
);

CREATE INDEX idx_cases_tenant ON cases (tenant_id);

CREATE INDEX idx_cases_status ON cases (status);

CREATE INDEX idx_cases_priority ON cases (priority);

CREATE INDEX idx_cases_customer ON cases (customer_id);

CREATE INDEX idx_cases_assigned ON cases (assigned_to);

CREATE INDEX idx_cases_sla ON cases (sla_due_at);

-- Case events (state machine history)
CREATE TABLE case_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    case_id UUID NOT NULL REFERENCES cases (id),
    event_type VARCHAR(50) NOT NULL,
    from_state VARCHAR(50),
    to_state VARCHAR(50),
    actor_id UUID,
    notes TEXT,
    metadata JSONB DEFAULT '{}',
    occurred_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_events_case ON case_events (case_id);

CREATE INDEX idx_case_events_occurred ON case_events (occurred_at);

-- Evidence references
CREATE TABLE evidence_refs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    case_id UUID NOT NULL REFERENCES cases (id),
    evidence_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    storage_path VARCHAR(1024) NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT,
    hash_sha256 VARCHAR(64),
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_evidence_case ON evidence_refs (case_id);

CREATE INDEX idx_evidence_type ON evidence_refs (evidence_type);