# Hafnium ML Inference Service
FROM python:3.11-slim

# Security: Create non-root user
RUN groupadd -g 1000 hafnium && \
    useradd -u 1000 -g hafnium -s /bin/false hafnium

WORKDIR /app

# Install dependencies
COPY pyproject.toml ./
RUN pip install --no-cache-dir ".[serving]"

# Copy application
COPY inference/ ./inference/
COPY pipelines/ ./pipelines/

# Copy models directory (create if build context has it)
# Note: models are typically mounted at runtime, not built into image

# Security: Run as non-root
USER hafnium

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD python -c "import httpx; httpx.get('http://localhost:8000/health').raise_for_status()"

EXPOSE 8000

ENTRYPOINT ["python", "-m", "inference.server"]
