# Hafnium Stream Processor

Kafka Streams-based stream processor for real-time event processing and enrichment.

## Overview

The stream processor consumes events from Kafka topics, performs real-time scoring and enrichment, and produces derived events for downstream consumers.

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Kafka     │────▶│   Processor  │────▶│   Kafka     │
│  (ingress)  │     │   (enrich)   │     │  (egress)   │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Scoring   │
                    │   Service   │
                    └─────────────┘
```

## Topics Processed

| Input Topic | Output Topic | Processing |
|-------------|--------------|------------|
| hf.txn.ingested.v1 | hf.txn.scored.v1 | Risk scoring |
| hf.customer.created.v1 | hf.customer.enriched.v1 | Customer enrichment |
| hf.alert.raised.v1 | hf.alert.enriched.v1 | Alert enrichment |

## Configuration

Configuration via environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| KAFKA_BOOTSTRAP_SERVERS | Kafka brokers | localhost:9092 |
| SCHEMA_REGISTRY_URL | Schema registry | http://localhost:8081 |
| APPLICATION_ID | Kafka Streams app ID | hafnium-stream-processor |
| SCORING_SERVICE_URL | Risk engine URL | http://localhost:8085 |

## Building

```bash
cd services/stream-processor
./gradlew build
```

## Running

```bash
./gradlew run
```

## License

Proprietary. All rights reserved.
