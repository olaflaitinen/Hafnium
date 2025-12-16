"""
Alert Rules Processor

Evaluates transaction monitoring rules and generates alerts.
"""

from datetime import datetime, timezone
from typing import Any, Dict, List
from uuid import uuid4


# Rule definitions
RULES = [
    {
        "rule_id": "RULE-001",
        "rule_name": "High Value Transaction",
        "severity": "high",
        "conditions": [
            {"field": "amount", "operator": "gt", "value": 10000},
        ],
    },
    {
        "rule_id": "RULE-002",
        "rule_name": "Unusual Transaction Velocity",
        "severity": "medium",
        "conditions": [
            {"field": "velocity_features.txn_count_24h", "operator": "gt", "value": 50},
        ],
    },
    {
        "rule_id": "RULE-003",
        "rule_name": "New Customer High Value",
        "severity": "high",
        "conditions": [
            {"field": "customer_profile.days_since_onboarding", "operator": "lt", "value": 30},
            {"field": "amount", "operator": "gt", "value": 5000},
        ],
    },
    {
        "rule_id": "RULE-004",
        "rule_name": "High Risk Score",
        "severity": "critical",
        "conditions": [
            {"field": "score", "operator": "gte", "value": 0.8},
        ],
    },
    {
        "rule_id": "RULE-005",
        "rule_name": "Unusual Geographic Pattern",
        "severity": "medium",
        "conditions": [
            {"field": "geo_data.country", "operator": "in", "value": ["HIGH_RISK_COUNTRIES"]},
        ],
    },
]

# High-risk country list (simplified)
HIGH_RISK_COUNTRIES = ["IR", "KP", "SY", "CU"]


async def evaluate_rules(
    enriched_event: Dict[str, Any],
    scored_event: Dict[str, Any],
) -> List[Dict[str, Any]]:
    """
    Evaluate all alert rules against the transaction.
    
    Args:
        enriched_event: Enriched transaction data
        scored_event: Scoring results
    
    Returns:
        List of alert events for triggered rules
    """
    alerts = []
    
    # Merge events for evaluation
    combined = {
        **enriched_event,
        "score": scored_event.get("score", 0),
        "risk_level": scored_event.get("risk_level", "LOW"),
    }
    
    for rule in RULES:
        triggered, conditions = evaluate_rule(rule, combined)
        
        if triggered:
            alert = create_alert(
                rule=rule,
                event=combined,
                score=scored_event.get("score", 0),
                triggered_conditions=conditions,
            )
            alerts.append(alert)
    
    return alerts


def evaluate_rule(
    rule: Dict[str, Any],
    event: Dict[str, Any],
) -> tuple[bool, List[Dict[str, Any]]]:
    """
    Evaluate a single rule against the event.
    
    Returns:
        Tuple of (triggered, triggered_conditions)
    """
    triggered_conditions = []
    
    for condition in rule["conditions"]:
        field = condition["field"]
        operator = condition["operator"]
        threshold = condition["value"]
        
        # Handle special placeholder values
        if threshold == ["HIGH_RISK_COUNTRIES"]:
            threshold = HIGH_RISK_COUNTRIES
        
        # Get field value from event (supports nested fields)
        actual_value = get_nested_value(event, field)
        
        if actual_value is None:
            return False, []
        
        # Evaluate condition
        if evaluate_condition(actual_value, operator, threshold):
            triggered_conditions.append({
                "condition": f"{field} {operator} {threshold}",
                "actual_value": str(actual_value),
                "threshold": str(threshold),
            })
        else:
            # All conditions must match (AND logic)
            return False, []
    
    return len(triggered_conditions) > 0, triggered_conditions


def get_nested_value(obj: Dict[str, Any], path: str) -> Any:
    """Get a value from a nested dictionary using dot notation."""
    keys = path.split(".")
    value = obj
    
    for key in keys:
        if isinstance(value, dict):
            value = value.get(key)
        else:
            return None
    
    return value


def evaluate_condition(actual: Any, operator: str, threshold: Any) -> bool:
    """Evaluate a single condition."""
    try:
        if operator == "gt":
            return actual > threshold
        elif operator == "gte":
            return actual >= threshold
        elif operator == "lt":
            return actual < threshold
        elif operator == "lte":
            return actual <= threshold
        elif operator == "eq":
            return actual == threshold
        elif operator == "ne":
            return actual != threshold
        elif operator == "in":
            return actual in threshold
        elif operator == "not_in":
            return actual not in threshold
        else:
            return False
    except (TypeError, ValueError):
        return False


def create_alert(
    rule: Dict[str, Any],
    event: Dict[str, Any],
    score: float,
    triggered_conditions: List[Dict[str, Any]],
) -> Dict[str, Any]:
    """Create an alert event."""
    return {
        "alert_id": str(uuid4()),
        "rule_id": rule["rule_id"],
        "rule_name": rule["rule_name"],
        "severity": rule["severity"],
        "score": score,
        "txn_id": event.get("txn_id"),
        "customer_id": event.get("customer_id"),
        "explanation": f"Transaction triggered rule: {rule['rule_name']}",
        "triggered_conditions": triggered_conditions,
        "created_at": datetime.now(timezone.utc).isoformat(),
    }
