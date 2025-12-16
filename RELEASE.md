# Release Information

**For the detailed release engineering process, see**: [docs/release/release-process.md](docs/release/release-process.md)

---

## Release Governance

Hafnium follows strictly Semantic Versioning (SemVer 2.0.0). All releases are cryptographically signed and immutable once published.

### Release Channels

| Channel | Tag Pattern | Stability | Audience | Frequency |
|---------|-------------|-----------|----------|-----------|
| **Stable** | `vX.Y.Z` | High | Production | Monthly |
| **Beta** | `vX.Y.Z-beta.N` | Medium | Staging / QA | Weekly |
| **Nightly** | `vX.Y.Z-dev.YYYYMMDD` | Low | Internal Dev | Daily |

### Artifact Verification

All official release artifacts (Docker images, JARs, Wheels) are signed. Verifying authenticity:

```bash
# Verify Docker image signature
cosign verify \
  --key cosign.pub \
  ghcr.io/olaflaitinen/hafnium/risk-engine:v1.2.0
```

## Latest Version

See the [GitHub Releases](https://github.com/olaflaitinen/hafnium/releases) page for the official changelogs and assets.
