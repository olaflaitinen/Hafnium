#!/bin/bash
set -e

echo "Bootstrapping Hafnium Local Environment..."

# 1. Create .env if not exists
if [ ! -f .env ]; then
    cp .env.example .env
    echo "Created .env from .env.example"
fi

# 2. Build Java Services
echo "Building Backend Services..."
./gradlew clean build -x test

# 3. Build Stream Processor
echo "Building Stream Processor..."
cd services/stream-processor
./gradlew clean build -x test
cd ../..

# 4. Create necessary directories for docker-compose volumes
mkdir -p .docker/postgres-data
mkdir -p .docker/redis-data
mkdir -p .docker/minio-data
mkdir -p .docker/redpanda-data

echo "Bootstrap complete. Run 'docker-compose up -d' to start."
