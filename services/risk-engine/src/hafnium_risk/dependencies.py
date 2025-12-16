"""
Dependency Injection

FastAPI dependency injection configuration.
"""

from typing import Optional

import redis.asyncio as redis
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

from hafnium_risk.config import get_settings
from hafnium_risk.services.risk_service import RiskService


# Global instances
_engine: Optional[object] = None
_session_factory: Optional[sessionmaker] = None
_redis_client: Optional[redis.Redis] = None
_risk_service: Optional[RiskService] = None


async def init_dependencies() -> None:
    """Initialize application dependencies."""
    global _engine, _session_factory, _redis_client, _risk_service
    
    settings = get_settings()
    
    # Database
    db_url = settings.database_url.replace("postgresql://", "postgresql+asyncpg://")
    _engine = create_async_engine(
        db_url,
        pool_size=settings.db_pool_size,
        max_overflow=settings.db_max_overflow,
    )
    _session_factory = sessionmaker(
        _engine,
        class_=AsyncSession,
        expire_on_commit=False,
    )
    
    # Redis
    _redis_client = redis.from_url(settings.redis_url, decode_responses=True)
    
    # Risk Service (placeholder for feature store and inference client)
    _risk_service = RiskService(
        feature_store=None,  # TODO: Initialize Feast client
        inference_client=MockInferenceClient(),
        cache=_redis_client,
        db=_session_factory,
    )


async def shutdown_dependencies() -> None:
    """Shutdown application dependencies."""
    global _engine, _redis_client
    
    if _engine:
        await _engine.dispose()
    
    if _redis_client:
        await _redis_client.close()


async def get_db() -> AsyncSession:
    """Get database session."""
    if _session_factory is None:
        raise RuntimeError("Dependencies not initialized")
    
    async with _session_factory() as session:
        yield session


def get_risk_service() -> RiskService:
    """Get risk service instance."""
    if _risk_service is None:
        raise RuntimeError("Dependencies not initialized")
    return _risk_service


class MockInferenceClient:
    """Mock inference client for development."""
    
    async def predict(
        self,
        model_id: str,
        features: dict,
    ) -> dict:
        """Mock prediction."""
        # Simple mock scoring logic
        score = 0.25
        
        if features.get("screening_matches", 0) > 0:
            score += 0.3
        
        if features.get("txn_count_24h", 0) > 10:
            score += 0.15
        
        if features.get("days_since_onboarding", 365) < 30:
            score += 0.2
        
        score = min(score, 1.0)
        
        return {
            "score": score,
            "reasons": [
                {
                    "code": "NORMAL_ACTIVITY",
                    "contribution": -0.1,
                    "description": "Transaction patterns within normal range",
                }
            ],
        }
