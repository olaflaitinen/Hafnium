"""
Risk Engine Configuration

Pydantic settings for application configuration.
"""

from functools import lru_cache
from typing import List

from pydantic import Field
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""
    
    # Application
    app_name: str = "hafnium-risk-engine"
    debug: bool = False
    log_level: str = "INFO"
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    
    # Database
    database_url: str = Field(
        default="postgresql://hafnium:hafnium_dev@localhost:5432/hafnium",
        description="PostgreSQL connection URL"
    )
    db_pool_size: int = 10
    db_max_overflow: int = 20
    
    # Redis
    redis_url: str = Field(
        default="redis://localhost:6379",
        description="Redis connection URL"
    )
    redis_ttl: int = 3600  # 1 hour default cache TTL
    
    # Kafka
    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_consumer_group: str = "risk-engine"
    
    # OPA
    opa_url: str = "http://localhost:8181/v1/data/hafnium/authz/allow"
    
    # Vault
    vault_addr: str = "http://localhost:8200"
    vault_token: str = ""
    
    # AI Inference
    inference_url: str = "http://localhost:8001"
    inference_timeout: float = 5.0
    
    # Feature Store
    feature_store_path: str = "feature_store/"
    
    # CORS
    cors_origins: List[str] = ["http://localhost:3000"]
    
    # OpenTelemetry
    otel_exporter_otlp_endpoint: str = "http://localhost:4317"
    otel_service_name: str = "risk-engine"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance."""
    return Settings()
