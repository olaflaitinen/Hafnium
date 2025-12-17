# NIST SSDF Controls Mapping

This document maps Hafnium security practices to NIST Secure Software Development Framework (SP 800-218).

---

## PO: Prepare the Organization

| Practice | Control | Implementation | Status |
|----------|---------|----------------|--------|
| PO.1.1 | Security requirements | Security policies in SECURITY.md | IMPLEMENTED |
| PO.1.2 | Security roles | CODEOWNERS, GOVERNANCE.md | IMPLEMENTED |
| PO.2.1 | Secure development training | Onboarding documentation | IMPLEMENTED |
| PO.3.1 | Tooling for security | CodeQL, Trivy, Gitleaks | IMPLEMENTED |
| PO.3.2 | Artifact integrity | SLSA provenance, Cosign signing | IMPLEMENTED |

---

## PS: Protect the Software

| Practice | Control | Implementation | Status |
|----------|---------|----------------|--------|
| PS.1.1 | Protect code from unauthorized access | Branch protection rules | IMPLEMENTED |
| PS.2.1 | Verify third-party provenance | Dependency-review action | IMPLEMENTED |
| PS.2.2 | SBOM for releases | CycloneDX/SPDX generation | IMPLEMENTED |
| PS.3.1 | Archive and protect releases | GitHub Releases + GHCR | IMPLEMENTED |
| PS.3.2 | Collect provenance data | SLSA attestations | IMPLEMENTED |

---

## PW: Produce Well-Secured Software

| Practice | Control | Implementation | Status |
|----------|---------|----------------|--------|
| PW.1.1 | Secure design principles | ADRs, threat model | IMPLEMENTED |
| PW.2.1 | Review software design | PR reviews, CODEOWNERS | IMPLEMENTED |
| PW.4.1 | SAST scanning | CodeQL workflow | IMPLEMENTED |
| PW.4.4 | Human code review | Required PR reviews | IMPLEMENTED |
| PW.5.1 | Secure coding practices | Pre-commit hooks, linting | IMPLEMENTED |
| PW.6.2 | Compiler security features | Non-root containers, drop caps | IMPLEMENTED |
| PW.7.1 | Security testing | Container scanning, OSV | IMPLEMENTED |
| PW.7.2 | Fuzz testing | Planned | PLANNED |
| PW.8.1 | Code-based vulnerabilities | Trivy, OSV Scanner | IMPLEMENTED |
| PW.8.2 | Executable vulnerabilities | Container image scanning | IMPLEMENTED |
| PW.9.1 | Secure build environments | GitHub Actions runners | IMPLEMENTED |
| PW.9.2 | Verify build integrity | SLSA L2 provenance | IMPLEMENTED |

---

## RV: Respond to Vulnerabilities

| Practice | Control | Implementation | Status |
|----------|---------|----------------|--------|
| RV.1.1 | Identify vulnerabilities | Dependabot, CodeQL alerts | IMPLEMENTED |
| RV.1.2 | Assess severity | CVSS scoring in alerts | IMPLEMENTED |
| RV.1.3 | Prioritize remediation | SLA by severity in SECURITY.md | IMPLEMENTED |
| RV.2.1 | Analyze root cause | Post-incident process | IMPLEMENTED |
| RV.2.2 | Correct vulnerabilities | Patch workflow | IMPLEMENTED |
| RV.3.1 | Report discoveries | SECURITY.md disclosure policy | IMPLEMENTED |
| RV.3.2 | Assist disclosures | Security contact defined | IMPLEMENTED |

---

## SLSA Level Alignment

| Level | Requirement | Implementation | Status |
|-------|-------------|----------------|--------|
| L1 | Provenance exists | Build attestations | IMPLEMENTED |
| L2 | Hosted build platform | GitHub Actions | IMPLEMENTED |
| L2 | Signed provenance | actions/attest-build-provenance | IMPLEMENTED |
| L3 | Isolated build | Ephemeral runners | PLANNED |

---

## Verification Commands

### Verify Container Signature

```bash
cosign verify ghcr.io/olaflaitinen/hafnium/identity-service:v1.0.0
```

### Verify SBOM

```bash
# Download SBOM from release
gh release download v1.0.0 -p 'sbom-*.json'

# Validate with grype
grype sbom:./sbom-full.cdx.json
```

### Verify Provenance

```bash
# Check attestation
gh attestation verify artifact.jar --owner olaflaitinen
```
