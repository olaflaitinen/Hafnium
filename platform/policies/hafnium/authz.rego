package hafnium.authz

# Hafnium Authorization Policies
# These policies define access control for the backend services.

default allow := false

# Allow if user is authorized for the action
allow if {
    input.tenant_id != ""
    input.user_id != ""
    user_has_permission
}

# Check if user has required role for the action
user_has_permission if {
    required_role := action_roles[input.action][input.resource]
    input.roles[_] == required_role
}

# Admin can do anything
user_has_permission if {
    input.roles[_] == "admin"
}

# Tenant scoping: ensure resource belongs to user's tenant
tenant_scoped if {
    input.resource_tenant_id == input.tenant_id
}

# Role mappings for actions
action_roles := {
    "read": {
        "customer": "analyst",
        "case": "analyst",
        "alert": "analyst",
        "transaction": "analyst",
        "screening": "analyst",
        "risk_score": "analyst"
    },
    "write": {
        "customer": "operator",
        "case": "analyst",
        "alert": "analyst",
        "transaction": "operator",
        "screening": "operator"
    },
    "delete": {
        "customer": "admin",
        "case": "admin",
        "alert": "admin"
    },
    "detokenize": {
        "vault": "admin"
    },
    "export": {
        "case": "compliance_officer"
    },
    "import": {
        "screening_list": "admin"
    }
}

# Sensitive operations require additional checks
sensitive_operation if {
    input.action == "detokenize"
}

sensitive_operation if {
    input.action == "export"
}

# Deny sensitive operations without explicit approval
deny if {
    sensitive_operation
    not explicit_approval
}

explicit_approval if {
    input.context.approval_id != ""
}
