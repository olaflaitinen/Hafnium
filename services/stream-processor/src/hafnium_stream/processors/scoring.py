"""
Transaction Scoring Processor

Calls the Risk Engine to score enriched transactions.
"""

from datetime import datetime, timezone
from typing import Any, Dict

import httpx

from hafnium_stream.config import Settings


settings = Settings()


async def score_transaction(enriched_event: Dict[str, Any]) -> Dict[str, Any]:
    """
    Score a transaction using the Risk Engine.
    
    Args:
        enriched_event: Enriched transaction event
    
    Returns:
        Scored transaction event
    """
    txn_id = enriched_event.get("txn_id")
    customer_id = enriched_event.get("customer_id")
    
    # Build feature vector from enriched event
    features = extract_features(enriched_event)
    
    # Call Risk Engine
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.risk_engine_url}/api/v1/risk/score",
                json={
                    "entity_type": "transaction",
                    "entity_id": txn_id,
                    "context": {
                        "use_case": "transaction_monitoring",
                        "amount": enriched_event.get("amount"),
                        "currency": enriched_event.get("currency"),
                    },
                    "features": features,
                },
                timeout=5.0,
            )
            response.raise_for_status()
            score_result = response.json()
        except httpx.HTTPError:
            # Fallback to simple rule-based scoring
            score_result = fallback_scoring(enriched_event)
    
    # Build scored event
    return {
        "txn_id": txn_id,
        "customer_id": customer_id,
        "score": score_result.get("score", 0.0),
        "risk_level": score_result.get("risk_level", "LOW"),
        "model_version": score_result.get("model_version", "fallback"),
        "reasons": score_result.get("reasons", []),
        "scored_at": datetime.now(timezone.utc).isoformat(),
    }


def extract_features(enriched_event: Dict[str, Any]) -> Dict[str, float]:
    """Extract numeric features from enriched event."""
    velocity = enriched_event.get("velocity_features", {})
    profile = enriched_event.get("customer_profile", {})
    network = enriched_event.get("network_features", {})
    
    return {
        "txn_amount": float(enriched_event.get("amount", 0)),
        "txn_count_24h": float(velocity.get("txn_count_24h", 0)),
        "txn_sum_24h": float(velocity.get("txn_sum_24h", 0)),
        "txn_avg_24h": float(velocity.get("txn_avg_24h", 0)),
        "days_since_onboarding": float(profile.get("days_since_onboarding", 0)),
        "total_txn_count": float(profile.get("total_txn_count", 0)),
        "counterparty_risk_score": float(network.get("counterparty_risk_score", 0)),
    }


def fallback_scoring(enriched_event: Dict[str, Any]) -> Dict[str, Any]:
    """
    Simple rule-based fallback scoring.
    
    Used when Risk Engine is unavailable.
    """
    score = 0.2  # Base score
    reasons = []
    
    amount = enriched_event.get("amount", 0)
    velocity = enriched_event.get("velocity_features", {})
    profile = enriched_event.get("customer_profile", {})
    
    # High amount
    if amount > 10000:
        score += 0.3
        reasons.append({
            "code": "HIGH_AMOUNT",
            "contribution": 0.3,
            "description": "Transaction amount exceeds threshold",
        })
    
    # High velocity
    if velocity.get("txn_count_24h", 0) > 20:
        score += 0.2
        reasons.append({
            "code": "HIGH_VELOCITY",
            "contribution": 0.2,
            "description": "High transaction frequency",
        })
    
    # New customer
    if profile.get("days_since_onboarding", 365) < 30:
        score += 0.15
        reasons.append({
            "code": "NEW_CUSTOMER",
            "contribution": 0.15,
            "description": "Customer recently onboarded",
        })
    
    score = min(score, 1.0)
    
    risk_level = "LOW"
    if score >= 0.8:
        risk_level = "CRITICAL"
    elif score >= 0.6:
        risk_level = "HIGH"
    elif score >= 0.3:
        risk_level = "MEDIUM"
    
    return {
        "score": score,
        "risk_level": risk_level,
        "model_version": "fallback:1.0.0",
        "reasons": reasons,
    }
