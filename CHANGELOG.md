# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial repository structure and governance files
- Docker Compose development stack with all platform services
- Risk Engine service with FastAPI and unified risk scoring
- AI Platform with Physics-Informed Neural Network (PINN) implementation
- Stream Processor with Faust for real-time transaction processing
- Java backend skeleton with Spring Boot
- React frontend skeleton with design system
- PostgreSQL schema with multi-tenant support and partitioning
- OPA authorization policies for RBAC and tenant isolation
- Keycloak realm configuration for authentication
- OpenAPI specifications for Risk Engine and Identity Service
- AsyncAPI specification for transaction events
- Prometheus, Loki, Tempo, and Grafana observability stack
- Envoy API gateway with rate limiting and JWT authentication
- GitHub Actions CI/CD pipeline
- Architecture Decision Records (ADRs)
- Runbooks and API documentation

### Security
- Implemented JWT authentication via Keycloak
- Added OPA-based fine-grained authorization
- Configured rate limiting in API gateway
- Set up row-level security (RLS) in PostgreSQL
- Added security scanning in CI pipeline

## [0.1.0] - 2025-12-16

### Added
- Repository initialization
- Design and Implementation Bible (DIB) documentation
- Repository specification document
