# Hafnium Libraries

This directory contains shared libraries used across Hafnium services.

## Structure

```
libs/
├── python/         # Python shared libraries
│   └── hafnium-common/
├── java/           # Java shared libraries
│   └── hafnium-common/
└── typescript/     # TypeScript shared libraries
    └── hafnium-common/
```

## Python Libraries

### hafnium-common

Shared utilities for Python services:

- Logging configuration
- Metrics utilities
- Common data models
- Configuration helpers

## Java Libraries

### hafnium-common

Shared utilities for Java services:

- Exception handling
- DTO base classes
- API response models

## TypeScript Libraries

### hafnium-common

Shared utilities for frontend:

- API client
- Authentication utilities
- Common types

---

**Status**: Planned

Libraries will be extracted from services as the codebase matures.
