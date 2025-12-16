# Hafnium Project Governance

This document describes the governance model for the Hafnium project.

---

## Overview

The Hafnium project operates under a meritocratic governance model where contributions and sustained engagement determine decision-making authority. This governance structure is designed to ensure project stability, security, and alignment with regulatory compliance objectives.

---

## Roles and Responsibilities

### Project Maintainers

Project Maintainers have write access to the repository and are responsible for:

- Reviewing and merging pull requests
- Triaging issues and security reports
- Releasing new versions
- Enforcing code quality standards
- Ensuring compliance with security policies

### Core Contributors

Core Contributors have demonstrated sustained, high-quality contributions and may:

- Propose architectural changes via ADRs
- Mentor new contributors
- Participate in design discussions
- Vote on significant project decisions

### Contributors

Contributors are individuals who have contributed to the project via:

- Code contributions
- Documentation improvements
- Bug reports and feature requests
- Community support

### Security Team

The Security Team is responsible for:

- Reviewing security-sensitive changes
- Responding to vulnerability reports
- Conducting security audits
- Maintaining threat models

---

## Decision-Making Process

### Routine Decisions

Routine decisions (bug fixes, minor improvements) are made by Maintainers through the standard pull request process.

### Significant Decisions

Significant decisions (breaking changes, new features, architectural changes) require:

1. An Architecture Decision Record (ADR)
2. Review period of at least 7 days
3. Approval from at least 2 Maintainers
4. No unresolved objections from Core Contributors

### Security Decisions

Security-related decisions require:

1. Security Team review
2. Threat model update (if applicable)
3. Approval from at least 1 Security Team member
4. Compliance impact assessment

---

## Contribution Path

```
Contributor → Core Contributor → Maintainer
```

### Becoming a Core Contributor

Requirements:
- Minimum 3 months of active contribution
- At least 10 merged pull requests
- Demonstrated understanding of project architecture
- Nomination by existing Maintainer

### Becoming a Maintainer

Requirements:
- Minimum 6 months as Core Contributor
- Demonstrated leadership in project activities
- Security clearance (if applicable)
- Unanimous approval from existing Maintainers

---

## Code of Conduct Enforcement

Violations of the [Code of Conduct](CODE_OF_CONDUCT.md) are handled as follows:

1. **Warning**: First-time minor violations
2. **Temporary Suspension**: Repeated or moderate violations
3. **Permanent Ban**: Severe violations or pattern of misconduct

Appeals may be submitted to the Project Maintainers.

---

## Release Process

Releases follow a structured process:

1. **Release Candidate**: Created from main branch
2. **Testing Period**: Minimum 7 days for major releases
3. **Security Review**: Required for all releases
4. **Changelog Update**: Document all changes
5. **Tag and Publish**: Following semantic versioning

See [docs/release/](docs/release/) for detailed release procedures.

---

## Amendments

This governance document may be amended by:

1. Proposal via pull request
2. 14-day discussion period
3. Approval from 2/3 of Maintainers
4. No unresolved objections

---

## Contact

For governance-related questions:

- Open a discussion on GitHub Discussions
- Contact the Maintainers via the security contact for sensitive matters

---

**DISCLAIMER**: This governance document does not create any legal obligations. The project is provided under the terms of the Apache-2.0 License with no warranty.

*Last Updated: 2025-12-16*
