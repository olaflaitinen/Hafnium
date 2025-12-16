# ADR-007: Model Serving Strategy

**Date**: 2025-12-16  
**Status**: Accepted  
**Deciders**: AI Platform Team

## Context
We need to serve trained PyTorch/PINN models with strict latency SLAs (< 50ms p99) and support canary deployments.

## Decision
We selected **BentoML** as the standard serving framework.

## Rationale
1.  **Standardization**: BentoML provides a unified artifact format ("Bento") that bundles code, model weights, and dependencies.
2.  **Performance**: Optimized runner architecture with adaptive batching.
3.  **Flexibility**: Supports HTTP and gRPC out of the box.

## Consequences
- **Positive**: Decoupled training from serving; standard Docker image generation.
- **Negative**: Additional learning curve for "Runner" concepts.
