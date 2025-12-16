# ADR-008: Feature Store Selection

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: Data Engineering Team

## Context

To prevent training-serving skew, we need a consistent way to serve features for both offline training and online inference.

## Decision

We selected **Feast** (Feature Store).

## Rationale

1. **Open Source**: Strong community support, no vendor lock-in.
2. **Architecture**: Minimalist architecture managing the offline (Parquet/S3) to online (Redis) sync.
3. **Integration**: Works well with our Python/Pandas stack.

## Consequences

- **Positive**: Point-in-time correctness for training data; low latency for online features.
- **Negative**: Operational overhead of maintaining the synchronization jobs.
