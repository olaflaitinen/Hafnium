"""
Risk Service

Core business logic for risk score computation.
"""

from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

from hafnium_risk.api.routes import (
    BatchRiskScoreResult,
    ModelCard,
    ModelInfo,
    PolicyAction,
    ReasonCode,
    RiskScoreRequest,
    RiskScoreResponse,
)


class RiskService:
    """
    Service for computing and managing risk scores.
    
    This service orchestrates:
    - Feature retrieval from the feature store
    - Model inference via the AI platform
    - Result caching and persistence
    - Explanation generation
    """
    
    RISK_LEVEL_THRESHOLDS = {
        "LOW": 0.3,
        "MEDIUM": 0.6,
        "HIGH": 0.8,
        "CRITICAL": 1.0,
    }
    
    POLICY_ACTIONS = {
        "LOW": [],
        "MEDIUM": [PolicyAction(action="FLAG", priority=1)],
        "HIGH": [
            PolicyAction(action="STEP_UP_AUTH", priority=1),
            PolicyAction(action="MANUAL_REVIEW", priority=2),
        ],
        "CRITICAL": [
            PolicyAction(action="BLOCK", priority=1),
            PolicyAction(action="MANUAL_REVIEW", priority=2),
        ],
    }
    
    def __init__(
        self,
        feature_store: Any,
        inference_client: Any,
        cache: Any,
        db: Any,
    ):
        self.feature_store = feature_store
        self.inference_client = inference_client
        self.cache = cache
        self.db = db
        self.model_version = "unified-risk:1.0.0"
    
    async def compute_score(
        self,
        entity_type: str,
        entity_id: str,
        context: Optional[Dict[str, Any]] = None,
        features: Optional[Dict[str, float]] = None,
    ) -> RiskScoreResponse:
        """
        Compute risk score for an entity.
        
        Args:
            entity_type: Type of entity (customer, transaction, session)
            entity_id: Unique identifier for the entity
            context: Additional context for scoring
            features: Pre-computed features (optional)
        
        Returns:
            RiskScoreResponse with score, level, reasons, and actions
        """
        # Get features if not provided
        if features is None:
            features = await self._get_features(entity_type, entity_id)
        
        # Add context features
        if context:
            features.update(self._extract_context_features(context))
        
        # Get inference from AI platform
        inference_result = await self.inference_client.predict(
            model_id="unified-risk",
            features=features,
        )
        
        score = inference_result["score"]
        risk_level = self._classify_risk_level(score)
        reasons = self._extract_reasons(inference_result)
        policy_actions = self.POLICY_ACTIONS.get(risk_level, [])
        
        computed_at = datetime.now(timezone.utc).isoformat()
        
        # Build response
        response = RiskScoreResponse(
            score=score,
            risk_level=risk_level,
            reasons=reasons,
            policy_actions=policy_actions,
            model_version=self.model_version,
            computed_at=computed_at,
        )
        
        # Cache the result
        await self._cache_score(entity_type, entity_id, response)
        
        # Persist to database
        await self._persist_score(entity_type, entity_id, response, features)
        
        return response
    
    async def get_cached_score(
        self,
        entity_type: str,
        entity_id: str,
    ) -> Optional[RiskScoreResponse]:
        """Get cached risk score for an entity."""
        cache_key = f"risk_score:{entity_type}:{entity_id}"
        cached = await self.cache.get(cache_key)
        if cached:
            return RiskScoreResponse.model_validate_json(cached)
        return None
    
    async def compute_score_batch(
        self,
        requests: List[RiskScoreRequest],
    ) -> List[BatchRiskScoreResult]:
        """Compute scores for multiple entities."""
        results = []
        for req in requests:
            try:
                result = await self.compute_score(
                    entity_type=req.entity_type,
                    entity_id=req.entity_id,
                    context=req.context.model_dump() if req.context else None,
                    features=req.features,
                )
                results.append(BatchRiskScoreResult(
                    entity_type=req.entity_type,
                    entity_id=req.entity_id,
                    result=result,
                ))
            except Exception as e:
                results.append(BatchRiskScoreResult(
                    entity_type=req.entity_type,
                    entity_id=req.entity_id,
                    error=str(e),
                ))
        return results
    
    async def list_models(self) -> List[ModelInfo]:
        """List available risk models."""
        return [
            ModelInfo(
                model_id="unified-risk",
                model_name="Unified Risk Scorer",
                version="1.0.0",
                status="active",
                created_at="2025-01-01T00:00:00Z",
            ),
            ModelInfo(
                model_id="aml-anomaly",
                model_name="AML Anomaly Detector",
                version="1.0.0",
                status="active",
                created_at="2025-01-01T00:00:00Z",
            ),
        ]
    
    async def get_model_card(self, model_id: str) -> Optional[ModelCard]:
        """Get model card for a specific model."""
        if model_id == "unified-risk":
            return ModelCard(
                model_id="unified-risk",
                name="Unified Risk Scorer",
                description="Ensemble model combining rules, XGBoost, and graph signals with PINN constraints",
                version="1.0.0",
                created_at="2025-01-01T00:00:00Z",
                intended_use="Real-time risk scoring for customers, transactions, and sessions",
                limitations=[
                    "Requires feature store data to be up-to-date",
                    "Performance degrades for entities with limited history",
                    "Not suitable for regulatory reporting without review",
                ],
                metrics={
                    "auc_roc": 0.94,
                    "auc_pr": 0.87,
                    "ece": 0.03,
                },
                training_data={
                    "description": "Historical transactions and outcomes",
                    "size": 1000000,
                    "date_range": "2023-01-01 to 2024-12-31",
                },
            )
        return None
    
    async def _get_features(
        self,
        entity_type: str,
        entity_id: str,
    ) -> Dict[str, float]:
        """Retrieve features from the feature store."""
        # Placeholder - integrate with Feast
        return {
            "txn_count_24h": 5,
            "txn_sum_24h": 1500.0,
            "txn_avg_24h": 300.0,
            "txn_count_7d": 25,
            "unique_counterparties_7d": 10,
            "days_since_onboarding": 180,
            "kyc_risk_score": 0.2,
            "screening_matches": 0,
        }
    
    def _extract_context_features(
        self,
        context: Dict[str, Any],
    ) -> Dict[str, float]:
        """Extract features from context."""
        features = {}
        if "amount" in context:
            features["txn_amount"] = float(context["amount"])
        return features
    
    def _classify_risk_level(self, score: float) -> str:
        """Classify score into risk level."""
        if score < self.RISK_LEVEL_THRESHOLDS["LOW"]:
            return "LOW"
        elif score < self.RISK_LEVEL_THRESHOLDS["MEDIUM"]:
            return "MEDIUM"
        elif score < self.RISK_LEVEL_THRESHOLDS["HIGH"]:
            return "HIGH"
        else:
            return "CRITICAL"
    
    def _extract_reasons(
        self,
        inference_result: Dict[str, Any],
    ) -> List[ReasonCode]:
        """Extract reason codes from inference result."""
        reasons = []
        for reason_data in inference_result.get("reasons", []):
            reasons.append(ReasonCode(
                code=reason_data["code"],
                contribution=reason_data["contribution"],
                description=reason_data["description"],
            ))
        return reasons
    
    async def _cache_score(
        self,
        entity_type: str,
        entity_id: str,
        response: RiskScoreResponse,
    ) -> None:
        """Cache the computed score."""
        cache_key = f"risk_score:{entity_type}:{entity_id}"
        await self.cache.set(
            cache_key,
            response.model_dump_json(),
            ex=3600,  # 1 hour TTL
        )
    
    async def _persist_score(
        self,
        entity_type: str,
        entity_id: str,
        response: RiskScoreResponse,
        features: Dict[str, float],
    ) -> None:
        """Persist score to database."""
        # Placeholder - implement database insert
        pass
