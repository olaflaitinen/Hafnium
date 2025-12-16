# ADR-001: Event-Driven Architecture

## Status

Accepted

## Context

The Hafnium platform requires processing of high-volume transaction data with low latency, while maintaining loose coupling between services. Traditional synchronous request-response patterns would create tight coupling and limit scalability.

Key requirements:

- Process millions of transactions per day
- Support real-time monitoring and alerting
- Enable independent scaling of components
- Maintain complete audit trail of all events
- Support event replay for debugging and recovery

## Decision

We will adopt an event-driven architecture using Apache Kafka (via Redpanda) as the central event backbone.

Key aspects:

1. **Event Sourcing**: State changes are captured as immutable events
2. **CQRS**: Separate read and write paths for optimal performance
3. **Saga Pattern**: Distributed transactions via choreography
4. **Schema Registry**: Avro schemas with compatibility enforcement

## Consequences

### Positive

- Services are loosely coupled and can evolve independently
- Events provide complete audit trail
- Horizontal scaling is straightforward
- Event replay enables debugging and recovery
- Supports both real-time and batch processing

### Negative

- Increased operational complexity
- Eventual consistency requires careful handling
- Debugging distributed flows is more complex
- Requires investment in observability

### Mitigations

- Comprehensive tracing with correlation IDs
- Dead letter queues for failed events
- Saga state persistence for recovery
- Clear event schema governance
