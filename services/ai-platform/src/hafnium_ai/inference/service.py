"""BentoML Inference Service for Risk Scoring."""

from __future__ import annotations

import bentoml
from pydantic import BaseModel, Field


class RiskScoreInput(BaseModel):
    """Input schema for risk scoring API."""

    entity_type: str = Field(description="Entity type (customer, transaction)")
    entity_id: str = Field(description="Entity identifier")
    transaction_amount: float = Field(ge=0, default=0)
    transaction_count_24h: int = Field(ge=0, default=0)
    avg_transaction_amount_30d: float = Field(ge=0, default=0)
    customer_age_days: int = Field(ge=0, default=365)
    country_risk_score: float = Field(ge=0, le=1, default=0)
    device_risk_score: float = Field(ge=0, le=1, default=0)
    velocity_ratio: float = Field(ge=0, default=1)
    network_risk_score: float = Field(ge=0, le=1, default=0)


class RiskScoreOutput(BaseModel):
    """Output schema for risk scoring API."""

    score: float
    risk_level: str
    reasons: list[dict]
    model_version: str


@bentoml.service(
    resources={"cpu": "500m", "memory": "512Mi"},
    traffic={"timeout": 30, "concurrency": 50},
)
class RiskScoringService:
    """BentoML service for risk scoring inference."""

    def __init__(self) -> None:
        from hafnium_ai.ml_models.risk_scoring import RiskScoringModel

        self.model = RiskScoringModel()

    @bentoml.api
    def predict(self, request: RiskScoreInput) -> RiskScoreOutput:
        """Compute risk score for the given entity."""
        from hafnium_ai.ml_models.risk_scoring import RiskFeatures

        features = RiskFeatures(
            transaction_amount=request.transaction_amount,
            transaction_count_24h=request.transaction_count_24h,
            avg_transaction_amount_30d=request.avg_transaction_amount_30d,
            customer_age_days=request.customer_age_days,
            country_risk_score=request.country_risk_score,
            device_risk_score=request.device_risk_score,
            velocity_ratio=request.velocity_ratio,
            network_risk_score=request.network_risk_score,
        )

        prediction = self.model.predict(features)

        return RiskScoreOutput(
            score=prediction.score,
            risk_level=prediction.risk_level,
            reasons=prediction.contributing_factors,
            model_version=prediction.model_version,
        )

    @bentoml.api
    def health(self) -> dict:
        """Health check endpoint."""
        return {"status": "healthy", "model_version": self.model.model_version}
