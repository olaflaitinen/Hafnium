# ADR-010: Flyway for Database Migrations

## Status

Accepted

## Context

The Hafnium backend requires a reliable database migration tool to manage schema changes across multiple services. The primary candidates are Flyway and Liquibase.

## Decision

We will use Flyway for database migrations.

## Rationale

1. **Native Spring Boot Integration**: Flyway has first-class support in Spring Boot with auto-configuration.
2. **SQL-First Approach**: Flyway uses plain SQL files, which are more transparent and easier to review.
3. **Simplicity**: Flyway has a simpler mental model compared to Liquibase's XML/YAML changelog approach.
4. **Performance**: Flyway migrations execute faster due to direct SQL execution.
5. **Team Familiarity**: SQL-based migrations are accessible to all team members.

## Consequences

### Positive

- Clear audit trail of database changes via versioned SQL files.
- Easy to understand and review migration scripts.
- Seamless integration with Spring Boot applications.
- Rollback supported via undo migrations (Flyway Teams).

### Negative

- Rollbacks require manual undo scripts (community edition).
- Less abstraction compared to Liquibase's database-agnostic format.

## Migration File Convention

- Location: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`
- Example: `V1__create_customers_table.sql`

## Schema Strategy

Each service uses a dedicated schema within a shared PostgreSQL database:

- `identity`: customers, kyc_workflows, documents
- `screening`: sanctions_entities, screening_matches
- `monitoring`: transactions, alerts, rules
- `case`: cases, case_events, evidence_refs
- `vault`: tokens, key_metadata
- `risk`: decisions, reason_codes
- `signals`: sessions, devices

This provides logical isolation while maintaining operational simplicity.
