# Contributing to Hafnium

Thank you for your interest in contributing to Hafnium. This document provides guidelines and instructions for contributing to this project.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Making Contributions](#making-contributions)
5. [Coding Standards](#coding-standards)
6. [Commit Guidelines](#commit-guidelines)
7. [Pull Request Process](#pull-request-process)
8. [Review Process](#review-process)
9. [Release Process](#release-process)

## Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details.

## Getting Started

### Prerequisites

Before contributing, ensure you have the following installed:

- Git 2.30 or later
- Docker 24.0 or later
- Docker Compose 2.20 or later
- GNU Make 4.0 or later
- Python 3.11 or later (for AI platform development)
- Java 21 or later (for backend development)
- Node.js 20 or later (for frontend development)

### Repository Access

1. Fork the repository on GitHub
2. Clone your fork locally:

   ```bash
   git clone git@github.com:YOUR_USERNAME/hafnium.git
   cd hafnium
   ```

3. Add the upstream remote:

   ```bash
   git remote add upstream git@github.com:olaflaitinen/hafnium.git
   ```

## Development Setup

### Initial Setup

```bash
# Install pre-commit hooks
make install-hooks

# Copy environment template
cp .env.example .env

# Start development stack
make up

# Verify setup
make test
```

### Service-Specific Setup

#### AI Platform (Python)

```bash
cd services/ai-platform
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -e ".[dev]"
```

#### Backend (Java)

```bash
cd services/backend-java
./gradlew build
```

#### Frontend (React)

```bash
cd services/frontend-react
npm install
```

## Making Contributions

### Contribution Types

We welcome the following types of contributions:

| Type | Description |
|------|-------------|
| Bug Fixes | Corrections to existing functionality |
| Features | New capabilities aligned with project roadmap |
| Documentation | Improvements to documentation |
| Tests | Additional test coverage |
| Performance | Performance optimizations |
| Security | Security improvements (see SECURITY.md for vulnerabilities) |

### Contribution Workflow

1. **Create an Issue**: For significant changes, create an issue first to discuss the approach
2. **Create a Branch**: Branch from `main` using the naming convention below
3. **Make Changes**: Implement your changes following coding standards
4. **Test**: Ensure all tests pass locally
5. **Commit**: Use conventional commit messages
6. **Push**: Push your branch to your fork
7. **Pull Request**: Open a pull request against `main`

### Branch Naming

Use the following branch naming convention:

```
<type>/<issue-number>-<description>

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- refactor: Code refactoring
- test: Test additions
- chore: Maintenance tasks

Examples:
- feat/123-add-risk-score-caching
- fix/456-correct-token-expiry
- docs/789-update-api-reference
```

## Coding Standards

### General Principles

1. **Clarity over Cleverness**: Write code that is easy to understand
2. **Consistent Style**: Follow established patterns in the codebase
3. **Strong Typing**: Use type annotations where available
4. **Documentation**: Document public APIs and complex logic
5. **Testing**: Write tests for new functionality

### Python Standards

- Follow PEP 8 with a line length of 100 characters
- Use type hints for function signatures
- Use docstrings following Google style
- Format with `black` and `isort`
- Lint with `ruff`

```python
def compute_risk_score(
    entity_id: str,
    features: dict[str, float],
    *,
    model_version: str | None = None,
) -> RiskScoreResult:
    """Compute risk score for an entity.

    Args:
        entity_id: Unique identifier for the entity.
        features: Feature dictionary for scoring.
        model_version: Optional specific model version to use.

    Returns:
        RiskScoreResult containing score and explanations.

    Raises:
        EntityNotFoundError: If entity does not exist.
        ModelNotFoundError: If specified model version is unavailable.
    """
    ...
```

### Java Standards

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Document public methods with Javadoc
- Format with `google-java-format`

```java
/**
 * Computes the risk score for a given entity.
 *
 * @param entityId the unique identifier for the entity
 * @param features the feature map for scoring
 * @return the computed risk score result
 * @throws EntityNotFoundException if the entity does not exist
 */
public RiskScoreResult computeRiskScore(
    String entityId, 
    Map<String, Double> features
) {
    ...
}
```

### TypeScript Standards

- Use TypeScript strict mode
- Define explicit types (avoid `any`)
- Use functional components for React
- Format with Prettier

```typescript
interface RiskScoreResult {
  score: number;
  riskLevel: RiskLevel;
  reasons: ReasonCode[];
}

async function computeRiskScore(
  entityId: string,
  features: Record<string, number>
): Promise<RiskScoreResult> {
  ...
}
```

## Commit Guidelines

We use Conventional Commits for clear and standardized commit messages.

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Description |
|------|-------------|
| feat | New feature |
| fix | Bug fix |
| docs | Documentation changes |
| style | Formatting, no code change |
| refactor | Code restructuring |
| perf | Performance improvement |
| test | Adding or updating tests |
| build | Build system changes |
| ci | CI configuration changes |
| chore | Maintenance tasks |

### Examples

```
feat(risk-engine): add caching for computed risk scores

Implement Redis-based caching for risk scores with configurable TTL.
Scores are cached with entity-type and entity-id as keys.

Closes #123
```

```
fix(identity-service): correct document expiry validation

The expiry validation was using local timezone instead of UTC,
causing incorrect rejections for documents in certain timezones.

Fixes #456
```

### Rules

1. Use imperative mood ("add feature" not "added feature")
2. Do not end the subject line with a period
3. Limit subject line to 72 characters
4. Separate subject from body with a blank line
5. Reference issues in the footer
6. No emojis in commit messages

## Pull Request Process

### Before Submitting

1. Ensure your branch is up to date with `main`:

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. Run all checks locally:

   ```bash
   make lint
   make test
   make typecheck
   ```

3. Update documentation if needed

### Pull Request Template

When creating a pull request, include:

1. **Description**: Clear explanation of changes
2. **Motivation**: Why these changes are needed
3. **Testing**: How changes were tested
4. **Breaking Changes**: Any breaking changes (if applicable)
5. **Checklist**: Completion of required items

### Review Criteria

Pull requests are evaluated against:

1. **Functionality**: Does it work as intended?
2. **Tests**: Is there adequate test coverage?
3. **Style**: Does it follow coding standards?
4. **Documentation**: Is documentation updated?
5. **Security**: Are there security implications?
6. **Performance**: Are there performance impacts?

## Review Process

### Reviewers

- Pull requests require at least one approval from a maintainer
- CODEOWNERS file defines required reviewers for specific paths
- Reviews should be completed within 3 business days

### Addressing Feedback

1. Respond to all review comments
2. Make requested changes in new commits
3. Re-request review when ready
4. Do not force-push after review has started

### Merging

- Maintainers merge approved pull requests
- Squash-merge is used to maintain clean history
- Branch is deleted after merge

## Release Process

Releases follow semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

See [RELEASE.md](docs/RELEASE.md) for detailed release procedures.

---

Thank you for contributing to Hafnium.
