"""
Stream Processor Configuration
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Stream processor settings."""
    
    # Kafka
    kafka_bootstrap_servers: str = "localhost:9092"
    schema_registry_url: str = "http://localhost:8085"
    topic_partitions: int = 8
    
    # Database
    database_url: str = "postgresql://hafnium:hafnium_dev@localhost:5432/hafnium"
    
    # Redis
    redis_url: str = "redis://localhost:6379"
    
    # Risk Engine
    risk_engine_url: str = "http://localhost:8000"
    
    # Logging
    log_level: str = "INFO"
    
    # OpenTelemetry
    otel_exporter_otlp_endpoint: str = "http://localhost:4317"
    
    class Config:
        env_file = ".env"
