package hafnium.authz

test_admin_can_do_anything if {
    allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["admin"],
        "action": "delete",
        "resource": "customer"
    }
}

test_analyst_can_read_customer if {
    allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["analyst"],
        "action": "read",
        "resource": "customer"
    }
}

test_analyst_cannot_delete_customer if {
    not allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["analyst"],
        "action": "delete",
        "resource": "customer"
    }
}

test_operator_can_write_transaction if {
    allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["operator"],
        "action": "write",
        "resource": "transaction"
    }
}

test_detokenize_requires_admin if {
    allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["admin"],
        "action": "detokenize",
        "resource": "vault"
    }
}

test_analyst_cannot_detokenize if {
    not allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["analyst"],
        "action": "detokenize",
        "resource": "vault"
    }
}

test_deny_without_tenant if {
    not allow with input as {
        "tenant_id": "",
        "user_id": "user-1",
        "roles": ["admin"],
        "action": "read",
        "resource": "customer"
    }
}

test_compliance_officer_can_export if {
    allow with input as {
        "tenant_id": "tenant-1",
        "user_id": "user-1",
        "roles": ["compliance_officer"],
        "action": "export",
        "resource": "case"
    }
}
