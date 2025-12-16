# Dependency Policy

## Overview

This policy governs the selection and management of third-party dependencies in Hafnium.

## Selection Criteria

-   **License Compatibility**: Must be compatible with Apache 2.0 (e.g., MIT, BSD, Apache). **GPL libraries are strictly prohibited.**
-   **Maintenance**: Must be actively maintained and have a responsive community.
-   **Security**: Must have no known critical vulnerabilities.
-   **Quality**: Must have decent test coverage and stability.

## Management

-   **Scanning**: All dependencies are scanned for vulnerabilities via OWASP Dependency Check and Trivy.
-   **Updates**: Dependencies should be kept up-to-date, preferably via automated tools like Dependabot/Renovate.
-   **Review**: New dependencies must be approved by a Maintainer.

## Prohibited Libraries

-   Libraries with restrictive licenses (GPL, AGPL)
-   Libraries with known unpatched security vulnerabilities
-   Abandoned libraries (no commits in >1 year)

