# ADR-006: Stream Processing Engine Selection

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Platform Architecture Team

## Context
Hafnium requires high-throughput, low-latency processing of transaction events for real-time fraud detection. The system needs to support stateful windowing (e.g., velocity checks) and join streams with static data (customer profiles).

## Decision
We selected **Faust** (Python) over Kafka Streams (Java) or Flink.

## Rationale
1.  **Language Unification**: Our Data Science team uses Python. Using Python for stream processing allows them to deploy models directly without rewriting logic in Java.
2.  **Simplicity**: Faust provides a simple actor-based concurrency model that is easier to reason about than Flink's sophisticated operator graph for our use case.
3.  **Ecosystem**: Native integration with `numpy` and `pandas` for feature engineering.

### Trade-offs
- **Performance**: Faust is slower than Flink/Java-native streams.
- **Maturity**: Less mature than the JVM ecosystem for Kafka.

## Consequences
- **Positive**: Rapid model deployment, shared code with Risk Engine.
- **Negative**: Vertical scaling limit is lower; requires more partitions for concurrency.
