# Dependency Policy

This document defines the policies for managing third-party dependencies in the Hafnium project.

---

## Overview

Effective dependency management is critical for security, maintainability, and compliance. This policy establishes standards for selecting, updating, and auditing dependencies across all Hafnium components.

---

## Dependency Selection Criteria

### Required Evaluation

Before adding a new dependency, evaluate:

| Criterion | Requirement |
|-----------|-------------|
| License | Must be Apache-2.0, MIT, BSD, or MPL-2.0 compatible |
| Maintenance | Active maintenance within last 6 months |
| Security | No unpatched critical vulnerabilities |
| Popularity | Minimum 100 GitHub stars or equivalent adoption |
| Documentation | Adequate documentation for intended use |

### Prohibited Dependencies

The following are prohibited:

- Dependencies with AGPL, GPL (without linking exception), or SSPL licenses
- Dependencies with known unpatched security vulnerabilities
- Dependencies from unverified or untrusted sources
- Dependencies that require network access during installation

---

## Version Pinning

### Python Dependencies

All Python dependencies must be pinned to exact versions in `pyproject.toml`:

```toml
dependencies = [
    "fastapi==0.109.0",
    "pydantic==2.5.3",
]
```

### Node.js Dependencies

Use exact versions in `package.json`:

```json
{
  "dependencies": {
    "react": "18.2.0",
    "axios": "1.6.5"
  }
}
```

### Java Dependencies

Pin versions in `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-web:3.2.1'
```

---

## Update Policy

### Regular Updates

| Update Type | Frequency | Review Required |
|-------------|-----------|-----------------|
| Security patches | Within 7 days | Maintainer |
| Minor versions | Monthly | Contributor |
| Major versions | Quarterly | 2 Maintainers |

### Update Process

1. Create branch for dependency updates
2. Run full test suite
3. Review changelog for breaking changes
4. Update SBOM
5. Submit pull request with justification

---

## Software Bill of Materials (SBOM)

### Generation

SBOMs are generated automatically on each release using:

- **Python**: `pip-licenses` and `cyclonedx-bom`
- **Node.js**: `cyclonedx-node`
- **Java**: `cyclonedx-gradle-plugin`

### Storage

SBOMs are stored in:
- `sbom/` directory in release artifacts
- GitHub Release attachments

### Format

SBOMs follow CycloneDX 1.5 specification in JSON format.

---

## Vulnerability Management

### Scanning

Automated vulnerability scanning runs on:

- Every pull request
- Daily scheduled scans
- Release preparation

### Tools

| Language | Tool |
|----------|------|
| Python | Safety, Snyk |
| Node.js | npm audit, Snyk |
| Java | OWASP Dependency Check |
| Containers | Trivy |

### Response

| Severity | Response Time | Action |
|----------|---------------|--------|
| Critical | 24 hours | Immediate patch or mitigation |
| High | 7 days | Patch in next release |
| Medium | 30 days | Include in regular update cycle |
| Low | 90 days | Address when convenient |

---

## License Compliance

### Allowed Licenses

| License | Status |
|---------|--------|
| Apache-2.0 | Allowed |
| MIT | Allowed |
| BSD-2-Clause | Allowed |
| BSD-3-Clause | Allowed |
| ISC | Allowed |
| MPL-2.0 | Allowed with review |
| LGPL-2.1+ | Allowed for dynamic linking only |

### Review Required

| License | Requirement |
|---------|-------------|
| MPL-2.0 | Legal review for modifications |
| LGPL | Confirm dynamic linking only |
| Custom | Legal review required |

### Prohibited

| License | Reason |
|---------|--------|
| GPL-2.0 (without exception) | Copyleft incompatibility |
| GPL-3.0 | Copyleft incompatibility |
| AGPL | Network copyleft |
| SSPL | Non-OSI approved |
| Proprietary | License conflict |

---

## Audit Trail

All dependency changes must be documented:

1. Commit message referencing dependency change
2. Entry in CHANGELOG.md for user-facing changes
3. SBOM update in release artifacts

---

## Exceptions

Exceptions to this policy require:

1. Written justification
2. Security Team review
3. Legal review (for license exceptions)
4. Approval from 2 Maintainers

---

## Enforcement

Automated checks enforce this policy:

- CI blocks PRs with prohibited licenses
- CI blocks PRs with critical vulnerabilities
- Pre-commit hooks validate dependency files

---

**DISCLAIMER**: This policy provides technical guidance only. Organizations must conduct their own legal and compliance review before adopting these practices.

*Last Updated: 2025-12-16*
