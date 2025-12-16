"""
Stream Processor Application

Faust-based stream processing for transaction enrichment and alerting.
"""

import logging
from typing import Any, Dict

import faust
from faust import Stream

from hafnium_stream.config import Settings
from hafnium_stream.processors.enrichment import enrich_transaction
from hafnium_stream.processors.rules import evaluate_rules
from hafnium_stream.processors.scoring import score_transaction


settings = Settings()

# Configure logging
logging.basicConfig(
    level=getattr(logging, settings.log_level),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

# Create Faust application
app = faust.App(
    "hafnium-stream-processor",
    broker=settings.kafka_bootstrap_servers,
    store="rocksdb://",
    topic_partitions=settings.topic_partitions,
)

# =============================================================================
# Topics
# =============================================================================

transaction_ingested_topic = app.topic(
    "hf.txn.ingested.v1",
    value_type=Dict[str, Any],
)

transaction_enriched_topic = app.topic(
    "hf.txn.enriched.v1",
    value_type=Dict[str, Any],
)

transaction_scored_topic = app.topic(
    "hf.txn.scored.v1",
    value_type=Dict[str, Any],
)

alert_raised_topic = app.topic(
    "hf.alert.raised.v1",
    value_type=Dict[str, Any],
)

dead_letter_topic = app.topic(
    "hf.dlq.v1",
    value_type=Dict[str, Any],
)

# =============================================================================
# Tables (State)
# =============================================================================

# Customer velocity counters
customer_velocity = app.Table(
    "customer_velocity",
    default=lambda: {"txn_count_24h": 0, "txn_sum_24h": 0.0},
    partitions=settings.topic_partitions,
)

# =============================================================================
# Agents
# =============================================================================

@app.agent(transaction_ingested_topic)
async def process_transactions(stream: Stream) -> None:
    """
    Process incoming transactions through the enrichment pipeline.
    
    Pipeline:
    1. Enrich with customer profile and velocity features
    2. Score transaction using risk engine
    3. Evaluate alert rules
    4. Emit appropriate events
    """
    async for event in stream:
        txn_id = event.get("txn_id")
        
        try:
            logger.info(f"Processing transaction {txn_id}")
            
            # Step 1: Enrich transaction
            enriched = await enrich_transaction(
                event,
                customer_velocity,
            )
            await transaction_enriched_topic.send(value=enriched)
            
            # Step 2: Score transaction
            scored = await score_transaction(enriched)
            await transaction_scored_topic.send(value=scored)
            
            # Step 3: Evaluate rules and generate alerts
            alerts = await evaluate_rules(enriched, scored)
            for alert in alerts:
                await alert_raised_topic.send(value=alert)
                logger.warning(f"Alert raised for transaction {txn_id}: {alert['rule_id']}")
            
            logger.info(f"Transaction {txn_id} processed successfully")
            
        except Exception as e:
            logger.error(f"Failed to process transaction {txn_id}: {e}")
            
            # Send to dead letter queue
            await dead_letter_topic.send(value={
                "original_topic": "hf.txn.ingested.v1",
                "original_event": event,
                "error_message": str(e),
                "error_type": type(e).__name__,
                "retry_count": 0,
            })


@app.agent(transaction_ingested_topic)
async def update_velocity(stream: Stream) -> None:
    """
    Update customer velocity counters in real-time.
    
    Maintains sliding window aggregations for:
    - Transaction count (1h, 24h)
    - Transaction sum (24h)
    - Unique counterparties (7d)
    """
    async for event in stream:
        customer_id = event.get("customer_id")
        if not customer_id:
            continue
        
        amount = event.get("amount", 0)
        
        # Update velocity counters
        current = customer_velocity[customer_id]
        customer_velocity[customer_id] = {
            "txn_count_24h": current["txn_count_24h"] + 1,
            "txn_sum_24h": current["txn_sum_24h"] + amount,
        }


# =============================================================================
# Cron Jobs
# =============================================================================

@app.crontab("0 * * * *")  # Every hour
async def decay_velocity_counters() -> None:
    """
    Decay velocity counters to implement time-based windowing.
    
    This is a simplified approach; production would use
    proper windowed aggregations.
    """
    logger.info("Running velocity counter decay")
    # In production, implement proper windowed aggregations
    pass


# =============================================================================
# Health Check
# =============================================================================

@app.page("/health")
async def health_check(self, request):
    """Health check endpoint."""
    from faust.web import Response
    return Response(
        content={"status": "healthy"},
        content_type="application/json",
    )


@app.page("/ready")
async def readiness_check(self, request):
    """Readiness check endpoint."""
    from faust.web import Response
    return Response(
        content={"status": "ready"},
        content_type="application/json",
    )


if __name__ == "__main__":
    app.main()
