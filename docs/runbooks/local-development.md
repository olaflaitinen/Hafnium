# Local Development Runbook

This guide details how to set up and run Hafnium locally for development.

---

## Prerequisites

The following tooling is required to contribute to the Hafnium codebase. Strict version adherence is enforced via `.tool-versions`.

| Tool | Required Version | Usage Profile |
|------|------------------|---------------|
| **Docker** | ≥ 24.0.0 | Runtime environment for all services |
| **Docker Compose** | ≥ 2.20.0 | Service orchestration |
| **Java JDK** | 21 (LTS) | Backend service development (Temurin distribution recommended) |
| **Python** | 3.11.x | Risk engine and AI platform development |
| **Node.js** | 20.x (LTS) | Frontend development |
| **Make** | ≥ 4.0 | Build automation interface |

---

## Quick Start

1. **Clone Repository**

    ```bash
    git clone git@github.com:olaflaitinen/hafnium.git
    cd hafnium
    ```

2. **Environment Setup**

    ```bash
    cp .env.example .env
    # Edit .env for any custom local settings
    ```

3. **Start Services**

    ```bash
    make up
    ```

    This launches all core services (Postgres, Redis, Kafka, Backend, Risk Engine) in background mode.

4. **Verify Status**

    ```bash
    make health
    ```

---

## Development Workflows

### 1. Backend Development (Java)

- **Port**: 8080 (API)
- **Debug Port**: 5005
- **Reload**:

    ```bash
    cd services/backend-java
    ./gradlew bootRun
    ```

### 2. Risk Engine Development (Python)

- **Port**: 8000
- **Virtual Env**:

    ```bash
    cd services/risk-engine
    python -m venv venv
    source venv/bin/activate
    pip install -e ".[dev]"
    ```

- **Run**: `uvicorn hafnium_risk.main:app --reload`

### 3. Frontend Development (React)

- **Port**: 3000
- **Setup**:

    ```bash
    cd services/frontend-react
    npm install
    npm start
    ```

---

## Testing

| Scope | Command | Description |
|-------|---------|-------------|
| **Unit** | `make test` | Run all unit tests |
| **Integration** | `make test-int` | Run containerized integration tests |
| **Lint** | `make lint` | Check code style and formatting |

---

## Troubleshooting

### Resetting Data

To wipe the database and start fresh:

```bash
make down
docker volume prune -f
make up
make seed
```

### Kafka Connectivity

If Kafka listeners fail, ensure `KAFKA_ADVERTISED_LISTENERS` in `docker-compose.yaml` matches your local network alias.
