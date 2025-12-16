# API Reference

This directory contains API documentation for the Hafnium platform.

## Services

| Service | Description | OpenAPI Spec |
|---------|-------------|--------------|
| Risk Engine | Unified risk scoring with explainability | [risk-engine.yaml](../../contracts/openapi/risk-engine.yaml) |
| Identity Service | Customer onboarding and KYC | [identity-service.yaml](../../contracts/openapi/identity-service.yaml) |
| Transaction Service | Transaction management | Coming soon |
| Alert Service | Alert management | Coming soon |
| Case Service | Investigation case management | Coming soon |

## Authentication

All APIs use OAuth 2.0 Bearer token authentication via Keycloak.

### Obtaining a Token

```bash
curl -X POST http://localhost:8081/realms/hafnium/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=hafnium-api" \
  -d "client_secret=change-me-in-production" \
  -d "username=analyst" \
  -d "password=analyst"
```

### Using the Token

```bash
curl -X GET http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer ${TOKEN}"
```

## Error Responses

All errors follow RFC 7807 Problem Details format:

```json
{
  "type": "https://hafnium.dev/errors/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "The request body contains invalid fields",
  "trace_id": "abc123def456",
  "errors": [
    {
      "field": "email",
      "code": "invalid_format",
      "message": "Email must be a valid email address"
    }
  ]
}
```

## Rate Limiting

API requests are rate limited:

| Tier | Limit | Window |
|------|-------|--------|
| Standard | 1000 requests | 1 minute |
| Burst | 100 requests | 1 second |

Rate limit headers are returned with each response:

- `RateLimit-Limit`: Request limit
- `RateLimit-Remaining`: Remaining requests
- `RateLimit-Reset`: Reset timestamp

## Pagination

List endpoints use cursor-based pagination:

```json
{
  "data": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6MTAwfQ==",
    "has_more": true
  }
}
```

Request next page:

```
GET /api/v1/customers?cursor=eyJpZCI6MTAwfQ==&limit=50
```
