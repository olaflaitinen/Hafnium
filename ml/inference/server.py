"""
Hafnium ML Inference Server

FastAPI-based inference service for risk scoring.
"""

import logging
import pickle
import time
from pathlib import Path
from typing import Any

import numpy as np
from fastapi import FastAPI, HTTPException
from prometheus_client import Counter, Histogram, make_asgi_app
from pydantic import BaseModel, Field

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Hafnium Risk Inference Service",
    description="ML inference for risk scoring",
    version="1.0.0",
)

# Prometheus metrics
INFERENCE_REQUESTS = Counter(
    "hafnium_ml_inference_requests_total",
    "Total inference requests",
    ["status"],
)
INFERENCE_LATENCY = Histogram(
    "hafnium_ml_inference_duration_seconds",
    "Inference latency in seconds",
    buckets=[0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0],
)

# Add Prometheus metrics endpoint
metrics_app = make_asgi_app()
app.mount("/metrics", metrics_app)


class ModelStore:
    """Singleton model store."""
    
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.model = None
            cls._instance.scaler = None
            cls._instance.model_id = None
        return cls._instance
    
    def load(self, model_path: Path):
        """Load model from path."""
        if model_path.is_dir():
            model_file = model_path / "latest"
            if model_file.is_symlink():
                model_file = model_path / model_file.resolve().name
        else:
            model_file = model_path
        
        with open(model_file, "rb") as f:
            artifacts = pickle.load(f)
        
        self.model = artifacts["model"]
        self.scaler = artifacts["scaler"]
        self.model_id = model_file.stem
        logger.info(f"Loaded model: {self.model_id}")


model_store = ModelStore()


class PredictionRequest(BaseModel):
    """Request schema for prediction."""
    entity_id: str = Field(..., description="Entity identifier")
    features: dict[str, float] = Field(..., description="Feature values")


class PredictionResponse(BaseModel):
    """Response schema for prediction."""
    entity_id: str
    risk_score: float
    risk_level: str
    model_id: str
    inference_time_ms: float


@app.on_event("startup")
async def startup():
    """Load model on startup."""
    model_path = Path("models/")
    if model_path.exists() and any(model_path.glob("*.pkl")):
        model_store.load(model_path)
    else:
        logger.warning("No model found. Inference will fail.")


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "model_loaded": model_store.model is not None,
        "model_id": model_store.model_id,
    }


@app.get("/ready")
async def ready():
    """Readiness check endpoint."""
    if model_store.model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")
    return {"status": "ready"}


@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """Make a risk prediction."""
    if model_store.model is None:
        INFERENCE_REQUESTS.labels(status="error").inc()
        raise HTTPException(status_code=503, detail="Model not loaded")
    
    start_time = time.perf_counter()
    
    try:
        feature_order = [
            "transaction_count_30d",
            "avg_amount_30d",
            "max_amount_30d",
            "unique_counterparties",
            "account_age_days",
            "country_risk_score",
        ]
        
        X = np.array([[request.features.get(f, 0.0) for f in feature_order]])
        X_scaled = model_store.scaler.transform(X)
        
        proba = model_store.model.predict_proba(X_scaled)[0, 1]
        risk_score = float(proba)
        
        if risk_score >= 0.75:
            risk_level = "critical"
        elif risk_score >= 0.5:
            risk_level = "high"
        elif risk_score >= 0.25:
            risk_level = "medium"
        else:
            risk_level = "low"
        
        inference_time = (time.perf_counter() - start_time) * 1000
        
        INFERENCE_REQUESTS.labels(status="success").inc()
        INFERENCE_LATENCY.observe(inference_time / 1000)
        
        return PredictionResponse(
            entity_id=request.entity_id,
            risk_score=risk_score,
            risk_level=risk_level,
            model_id=model_store.model_id or "unknown",
            inference_time_ms=inference_time,
        )
        
    except Exception as e:
        INFERENCE_REQUESTS.labels(status="error").inc()
        logger.error(f"Inference error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


def main():
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)


if __name__ == "__main__":
    main()
