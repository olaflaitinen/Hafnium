"""
Risk Engine API Routes

FastAPI router definitions for the risk scoring API.
"""

from typing import List, Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field

from hafnium_risk.dependencies import get_risk_service
from hafnium_risk.services.risk_service import RiskService


router = APIRouter(tags=["risk"])


# =============================================================================
# Request/Response Models
# =============================================================================

class RiskScoreContext(BaseModel):
    """Additional context for risk scoring."""
    use_case: Optional[str] = None
    amount: Optional[float] = None
    currency: Optional[str] = None


class RiskScoreRequest(BaseModel):
    """Request model for risk score computation."""
    entity_type: str = Field(..., description="Type of entity to score")
    entity_id: str = Field(..., description="Unique identifier of the entity")
    context: Optional[RiskScoreContext] = None
    features: Optional[dict[str, float]] = None


class ReasonCode(BaseModel):
    """A contributing factor to the risk score."""
    code: str
    contribution: float
    description: str


class PolicyAction(BaseModel):
    """A recommended policy action based on risk level."""
    action: str
    priority: int


class RiskScoreResponse(BaseModel):
    """Response model for risk score computation."""
    score: float = Field(..., ge=0, le=1)
    risk_level: str
    reasons: List[ReasonCode]
    policy_actions: List[PolicyAction]
    model_version: str
    computed_at: str


class BatchRiskScoreRequest(BaseModel):
    """Request model for batch risk scoring."""
    requests: List[RiskScoreRequest] = Field(..., max_length=100)


class BatchRiskScoreResult(BaseModel):
    """Result for a single entity in batch scoring."""
    entity_type: str
    entity_id: str
    result: Optional[RiskScoreResponse] = None
    error: Optional[str] = None


class BatchRiskScoreResponse(BaseModel):
    """Response model for batch risk scoring."""
    results: List[BatchRiskScoreResult]


class ModelInfo(BaseModel):
    """Information about a risk model."""
    model_id: str
    model_name: str
    version: str
    status: str
    created_at: str


class ModelCard(BaseModel):
    """Detailed model card for a risk model."""
    model_id: str
    name: str
    description: str
    version: str
    created_at: str
    intended_use: str
    limitations: List[str]
    metrics: dict
    training_data: dict


# =============================================================================
# Routes
# =============================================================================

@router.post("/risk/score", response_model=RiskScoreResponse)
async def compute_risk_score(
    request: RiskScoreRequest,
    risk_service: RiskService = Depends(get_risk_service),
) -> RiskScoreResponse:
    """
    Compute risk score for an entity.
    
    This endpoint computes a unified risk score using the ensemble model,
    which aggregates signals from rules, ML models, and graph analysis.
    """
    try:
        result = await risk_service.compute_score(
            entity_type=request.entity_type,
            entity_id=request.entity_id,
            context=request.context.model_dump() if request.context else None,
            features=request.features,
        )
        return result
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        )


@router.get("/risk/score/{entity_type}/{entity_id}", response_model=RiskScoreResponse)
async def get_risk_score(
    entity_type: str,
    entity_id: str,
    risk_service: RiskService = Depends(get_risk_service),
) -> RiskScoreResponse:
    """
    Retrieve the most recent risk score for an entity.
    
    Returns 404 if no score has been computed for this entity.
    """
    result = await risk_service.get_cached_score(
        entity_type=entity_type,
        entity_id=entity_id,
    )
    if result is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"No score found for {entity_type}/{entity_id}",
        )
    return result


@router.post("/risk/score/batch", response_model=BatchRiskScoreResponse)
async def compute_risk_score_batch(
    request: BatchRiskScoreRequest,
    risk_service: RiskService = Depends(get_risk_service),
) -> BatchRiskScoreResponse:
    """
    Compute risk scores for multiple entities.
    
    Up to 100 entities can be scored in a single request.
    Failures for individual entities do not fail the entire batch.
    """
    results = await risk_service.compute_score_batch(request.requests)
    return BatchRiskScoreResponse(results=results)


@router.get("/risk/models", response_model=List[ModelInfo])
async def list_models(
    risk_service: RiskService = Depends(get_risk_service),
) -> List[ModelInfo]:
    """List available risk models."""
    return await risk_service.list_models()


@router.get("/risk/models/{model_id}/card", response_model=ModelCard)
async def get_model_card(
    model_id: str,
    risk_service: RiskService = Depends(get_risk_service),
) -> ModelCard:
    """Get model card for a specific model."""
    card = await risk_service.get_model_card(model_id)
    if card is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Model not found: {model_id}",
        )
    return card
