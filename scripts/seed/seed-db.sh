#!/usr/bin/env bash
# Database seeding script

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Default values
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-hafnium}"
DB_USER="${DB_USER:-hafnium}"
DB_PASSWORD="${DB_PASSWORD:-hafnium_dev}"

# Colors
GREEN='\033[0;32m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

seed_tenants() {
    log_info "Seeding tenants..."
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF
INSERT INTO tenants (tenant_id, name, status, settings)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Demo Bank', 'active', '{"tier": "enterprise"}'),
    ('00000000-0000-0000-0000-000000000002', 'Test Credit Union', 'active', '{"tier": "standard"}')
ON CONFLICT (tenant_id) DO NOTHING;
EOF
}

seed_customers() {
    log_info "Seeding customers..."
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF
INSERT INTO customers (customer_id, tenant_id, external_id, customer_type, status, risk_tier)
VALUES 
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'CUST-001', 'individual', 'verified', 'low'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'CUST-002', 'individual', 'verified', 'medium'),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'CUST-003', 'business', 'in_review', 'high')
ON CONFLICT (customer_id) DO NOTHING;
EOF
}

seed_transactions() {
    log_info "Seeding transactions..."
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF
INSERT INTO transactions (txn_id, tenant_id, customer_id, amount, currency, txn_type, status, txn_timestamp)
VALUES 
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 1000.00, 'USD', 'credit', 'completed', NOW() - INTERVAL '1 day'),
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 2500.00, 'USD', 'debit', 'completed', NOW() - INTERVAL '12 hours'),
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 15000.00, 'USD', 'transfer', 'pending', NOW())
ON CONFLICT (txn_id) DO NOTHING;
EOF
}

main() {
    log_info "Starting database seeding..."
    
    export PGPASSWORD="$DB_PASSWORD"
    
    seed_tenants
    seed_customers
    seed_transactions
    
    log_info "Database seeding complete!"
}

main "$@"
