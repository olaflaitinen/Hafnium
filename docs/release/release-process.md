# Release Process

This document describes the release process for the Hafnium platform.

---

## Versioning

### Semantic Versioning

Hafnium follows [Semantic Versioning 2.0.0](https://semver.org/):

```
MAJOR.MINOR.PATCH

Example: 1.2.3
```

| Component | When to Increment |
|-----------|-------------------|
| MAJOR | Breaking API changes, major architectural changes |
| MINOR | New features, non-breaking enhancements |
| PATCH | Bug fixes, security patches |

### Pre-release Versions

```
1.2.3-alpha.1  # Alpha release
1.2.3-beta.1   # Beta release
1.2.3-rc.1     # Release candidate
```

---

## Release Schedule

### Regular Releases

| Release Type | Frequency | Scope |
|--------------|-----------|-------|
| Major | Annually | Breaking changes |
| Minor | Monthly | Features |
| Patch | As needed | Bug fixes |
| Security | As needed | Security patches |

### Release Windows

- **Feature Freeze**: 2 weeks before release
- **Code Freeze**: 1 week before release
- **Release Day**: Tuesday (preferred)

---

## Release Workflow

### 1. Pre-Release

```
1.1 Create release branch
    git checkout -b release/v1.2.0

1.2 Update version numbers
    - pyproject.toml (all Python services)
    - package.json (frontend)
    - build.gradle (backend)

1.3 Update CHANGELOG.md
    - Move Unreleased to new version section
    - Add release date

1.4 Run release dry-run
    gh workflow run release-dry-run.yaml

1.5 Create release PR
    - Title: "chore(release): prepare v1.2.0"
    - Request reviews
```

### 2. Release Approval

```
2.1 Required approvals:
    - 2 maintainers
    - Security team (for major/minor)
    - Compliance review (if applicable)

2.2 Merge release PR to main
```

### 3. Create Release

```
3.1 Create and push tag
    git tag -a v1.2.0 -m "Release v1.2.0"
    git push origin v1.2.0

3.2 GitHub Release
    - Create release from tag
    - Copy CHANGELOG entry to release notes
    - Attach artifacts (SBOM, checksums)

3.3 Publish artifacts
    - Docker images to registry
    - Python packages (if public)
    - Helm charts
```

### 4. Post-Release

```
4.1 Announce release
    - Internal communication
    - GitHub Discussions

4.2 Monitor deployments
    - Watch for issues
    - Be ready to hotfix

4.3 Update documentation
    - Update version references
    - Archive old documentation
```

---

## Hotfix Process

### Security Hotfixes

```
1. Create hotfix branch from latest release tag
   git checkout -b hotfix/v1.2.1 v1.2.0

2. Apply minimal fix
   - Security fix only
   - No feature changes

3. Expedited review
   - Security team required
   - 1 maintainer minimum

4. Release immediately
   - Follow standard release steps
   - Prioritize production deployment
```

---

## Artifact Checklist

### Release Artifacts

| Artifact | Location | Required |
|----------|----------|----------|
| Docker images | Container registry | Yes |
| Helm charts | Helm repository | Yes |
| SBOM | GitHub Release | Yes |
| Checksums | GitHub Release | Yes |
| Release notes | GitHub Release | Yes |

### Documentation Updates

| Document | Update Required |
|----------|-----------------|
| CHANGELOG.md | Yes |
| README.md | If applicable |
| API documentation | If API changed |
| Migration guide | If breaking changes |

---

## Rollback Procedure

### Immediate Rollback

```bash
# Kubernetes
kubectl rollout undo deployment/risk-engine -n hafnium-system

# Docker Compose
docker compose down
docker compose -f docker-compose.yaml up -d --no-build
```

### Version-Specific Rollback

```bash
# Deploy specific version
helm upgrade hafnium hafnium/hafnium \
  --set global.image.tag=v1.1.0 \
  --namespace hafnium-system
```

---

## Communication

### Internal Channels

- Release planning: Weekly engineering sync
- Release announcements: Engineering Slack channel
- Incident communication: Incident channel

### External Channels

- GitHub Releases
- GitHub Discussions

---

**DISCLAIMER**: This release process is provided as guidance. Organizations must adapt it to their specific operational requirements.

*Last Updated: 2025-12-16*
