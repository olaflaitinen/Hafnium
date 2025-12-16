# Hafnium Authorization Policies
# Open Policy Agent (OPA) Rego policies

package hafnium.authz

import rego.v1

# Default deny
default allow := false

# Allow if user is authenticated and authorized
allow if {
    valid_token
    authorized
}

# Token validation
valid_token if {
    input.claims.exp > time.now_ns() / 1000000000
    input.claims.iss == "http://keycloak:8080/realms/hafnium"
}

# Authorization rules
authorized if {
    has_required_role
    tenant_access_allowed
}

# Role-based access control
has_required_role if {
    required_roles := role_requirements[input.method][input.path]
    user_roles := input.claims.realm_access.roles
    some required_role in required_roles
    required_role in user_roles
}

# Tenant isolation
tenant_access_allowed if {
    # User can only access resources in their tenant
    input.resource_tenant == input.claims.tenant_id
}

# Admin can access all tenants
tenant_access_allowed if {
    "admin" in input.claims.realm_access.roles
}

# Role requirements by endpoint
role_requirements := {
    "GET": {
        "/api/v1/customers": ["analyst", "admin"],
        "/api/v1/customers/*": ["analyst", "admin"],
        "/api/v1/alerts": ["analyst", "admin"],
        "/api/v1/cases": ["analyst", "admin"],
        "/api/v1/risk/score": ["service", "admin"],
    },
    "POST": {
        "/api/v1/customers": ["operator", "admin"],
        "/api/v1/risk/score": ["service", "admin"],
        "/api/v1/cases": ["analyst", "admin"],
    },
    "PUT": {
        "/api/v1/customers/*": ["operator", "admin"],
        "/api/v1/cases/*": ["analyst", "admin"],
    },
    "DELETE": {
        "/api/v1/customers/*": ["admin"],
        "/api/v1/cases/*": ["admin"],
    },
}

# Service-to-service authentication
allow if {
    input.claims.client_id != null
    input.claims.client_id in allowed_services
    service_scope_allowed
}

allowed_services := [
    "risk-engine",
    "backend-java",
    "ai-inference",
    "stream-processor",
]

service_scope_allowed if {
    required_scope := service_scopes[input.claims.client_id][input.path]
    required_scope in input.claims.scope
}

service_scopes := {
    "risk-engine": {
        "/api/v1/customers/*": "read:customers",
        "/api/v1/risk/score": "write:risk",
    },
    "backend-java": {
        "/api/v1/risk/score": "read:risk",
    },
}
