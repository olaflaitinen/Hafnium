"""Hafnium AI Platform - Core Risk Scoring Model."""

from typing import Any

import numpy as np
import torch
import torch.nn as nn
from pydantic import BaseModel, Field


class RiskFeatures(BaseModel):
    """Input features for risk scoring."""

    transaction_amount: float = Field(ge=0)
    transaction_count_24h: int = Field(ge=0)
    avg_transaction_amount_30d: float = Field(ge=0)
    customer_age_days: int = Field(ge=0)
    country_risk_score: float = Field(ge=0, le=1)
    device_risk_score: float = Field(ge=0, le=1)
    velocity_ratio: float = Field(ge=0)
    network_risk_score: float = Field(ge=0, le=1)


class RiskPrediction(BaseModel):
    """Risk scoring output."""

    score: float = Field(ge=0, le=1)
    risk_level: str
    contributing_factors: list[dict[str, Any]]
    model_version: str


class RiskScoringNetwork(nn.Module):
    """Neural network for risk score calibration."""

    def __init__(self, input_dim: int = 8, hidden_dim: int = 64) -> None:
        super().__init__()
        self.network = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.ReLU(),
            nn.BatchNorm1d(hidden_dim),
            nn.Dropout(0.2),
            nn.Linear(hidden_dim, hidden_dim // 2),
            nn.ReLU(),
            nn.BatchNorm1d(hidden_dim // 2),
            nn.Linear(hidden_dim // 2, 1),
            nn.Sigmoid(),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.network(x)


class RiskScoringModel:
    """Combined rule-based and ML risk scoring model."""

    def __init__(self, model_path: str | None = None) -> None:
        self.model_version = "v1.0.0"
        self.network = RiskScoringNetwork()
        self.network.eval()

        if model_path:
            self._load_weights(model_path)

    def _load_weights(self, path: str) -> None:
        """Load model weights from file."""
        state_dict = torch.load(path, map_location="cpu", weights_only=True)
        self.network.load_state_dict(state_dict)

    def _extract_features(self, features: RiskFeatures) -> np.ndarray:
        """Extract and normalize features."""
        return np.array([
            np.log1p(features.transaction_amount) / 15.0,
            min(features.transaction_count_24h / 50.0, 1.0),
            np.log1p(features.avg_transaction_amount_30d) / 15.0,
            min(features.customer_age_days / 365.0, 1.0),
            features.country_risk_score,
            features.device_risk_score,
            min(features.velocity_ratio / 10.0, 1.0),
            features.network_risk_score,
        ], dtype=np.float32)

    def _compute_rule_score(self, features: RiskFeatures) -> tuple[float, list[dict]]:
        """Compute rule-based risk score."""
        contributing_factors = []
        score = 0.0

        if features.transaction_amount > 10000:
            factor = min((features.transaction_amount - 10000) / 90000, 0.3)
            score += factor
            contributing_factors.append({
                "factor": "high_value_transaction",
                "contribution": factor,
            })

        if features.velocity_ratio > 3.0:
            factor = min((features.velocity_ratio - 3.0) / 7.0 * 0.25, 0.25)
            score += factor
            contributing_factors.append({
                "factor": "high_velocity",
                "contribution": factor,
            })

        if features.customer_age_days < 30:
            factor = (30 - features.customer_age_days) / 30 * 0.15
            score += factor
            contributing_factors.append({
                "factor": "new_customer",
                "contribution": factor,
            })

        if features.country_risk_score > 0.5:
            factor = (features.country_risk_score - 0.5) * 0.4
            score += factor
            contributing_factors.append({
                "factor": "country_risk",
                "contribution": factor,
            })

        return min(score, 1.0), contributing_factors

    def predict(self, features: RiskFeatures) -> RiskPrediction:
        """Compute risk score combining rules and ML."""
        rule_score, contributing_factors = self._compute_rule_score(features)

        feature_vector = self._extract_features(features)
        with torch.no_grad():
            tensor = torch.from_numpy(feature_vector).unsqueeze(0)
            ml_score = self.network(tensor).item()

        final_score = 0.6 * ml_score + 0.4 * rule_score

        if final_score >= 0.8:
            risk_level = "CRITICAL"
        elif final_score >= 0.6:
            risk_level = "HIGH"
        elif final_score >= 0.4:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        return RiskPrediction(
            score=round(final_score, 4),
            risk_level=risk_level,
            contributing_factors=contributing_factors,
            model_version=self.model_version,
        )
