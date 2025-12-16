"""
Risk Engine Main Application

FastAPI application for the unified risk scoring service.
"""

from contextlib import asynccontextmanager
from typing import AsyncGenerator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator

from hafnium_risk.api.routes import router
from hafnium_risk.config import Settings
from hafnium_risk.dependencies import init_dependencies, shutdown_dependencies


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """Application lifespan manager."""
    # Startup
    await init_dependencies()
    yield
    # Shutdown
    await shutdown_dependencies()


def create_app() -> FastAPI:
    """Create and configure the FastAPI application."""
    settings = Settings()
    
    app = FastAPI(
        title="Hafnium Risk Engine",
        description="Unified risk scoring with explainability",
        version="1.0.0",
        docs_url="/docs" if settings.debug else None,
        redoc_url="/redoc" if settings.debug else None,
        lifespan=lifespan,
    )
    
    # CORS middleware
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    # Prometheus metrics
    Instrumentator().instrument(app).expose(app)
    
    # Include API routes
    app.include_router(router, prefix="/api/v1")
    
    # Health check
    @app.get("/health")
    async def health() -> dict:
        return {"status": "healthy"}
    
    @app.get("/ready")
    async def ready() -> dict:
        # Add readiness checks here
        return {"status": "ready"}
    
    return app


app = create_app()
