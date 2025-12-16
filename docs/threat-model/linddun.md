# LINDDUN Privacy Analysis

**Methodology**: LINDDUN (Linkability, Identifiability, Non-repudiation, Detectability, Disclosure, Unawareness, Non-compliance)  
**Scope**: Customer Personal Data (PII)

---

## 1. Linkability

**Threat**: Attacker links two anonymized datasets to re-identify users.

| Context | Scenario | Mitigation |
|---------|----------|------------|
| **Transaction Data** | Correlating amounts/times with external leaks | Differential Privacy noise injection |
| **Sessions** | Tracking across devices | Session IDs rotated on login |

---

## 2. Identifiability

**Threat**: Identifying a subject from a set of data items.

| Context | Scenario | Mitigation |
|---------|----------|------------|
| **Logs** | User ID visibility | Use Pseudonymous IDs (UUIDs) in logs |
| **Analytics** | Unique device fingerprinting | Standardization of User-Agent |

---

## 3. Non-repudiation

**Threat**: Attacker cannot deny a claim (Inverse of Security Repudiation - *Privacy goal is plausibility*).

*Note: In a regulated financial context, Non-repudiation is often a requirement, not a threat. We prioritize Repudiation protection for audit trails over Privacy Non-repudiation.*

---

## 4. Detectability

**Threat**: Distinguishing whether an item is in the dataset or not.

| Context | Scenario | Mitigation |
|---------|----------|------------|
| **Model Inversion** | Checking if a person verified | Membership Inference Attack defense |

---

## 5. Disclosure of Information

**Threat**: Excessive collection or exposure.

| Context | Scenario | Mitigation |
|---------|----------|------------|
| **KYC** | Storing raw ID images forever | Auto-deletion after retention period |
| **API** | Sending full profile on refresh | GraphQL / Field selection |

---

## 6. Unawareness

**Threat**: User is unaware of data processing.

| Context | Scenario | Mitigation |
|---------|----------|------------|
| **Profiling** | Automated Risk Scoring | Explicit Privacy Notice & Consent |
| **3rd Party** | Sharing with vendors | Vendor list in Privacy Policy |

---

## 7. Non-compliance

**Threat**: Processing violates GDPR/CCPA.

| Regulation | Requirement | Control |
|------------|-------------|---------|
| **GDPR** | Right to be Forgotten | `DELETE /api/v1/customers/{id}` cascades |
| **CCPA** | Data Portability | `GET /api/v1/customers/{id}/export` |
