# KYC/AML Compliance Mapping

This document maps the Hafnium platform's technical controls to key Know Your Customer (KYC) and Anti-Money Laundering (AML) regulations.

---

## Regulatory Frameworks

- **USA**: Bank Secrecy Act (BSA), USA PATRIOT Act
- **EU**: 4th, 5th, 6th AML Directives (AMLD4/5/6)
- **UK**: Money Laundering Regulations 2017
- **Singapore**: MAS Notice 626

---

## Control Mapping Matrix

| Regulation ID | Requirement | Technical Control | Implementation | Verification |
|---------------|-------------|-------------------|----------------|--------------|
| **CIP (BSA)** | Customer Identification Program | Identity Verification API | `POST /api/v1/customers/kyc` | Automated Test |
| **CDD (AMLD5)** | Customer Due Diligence | Risk Scoring Engine | `RiskEngine.calculate_score()` | Unit Test |
| **EDD (AMLD5)** | Enhanced Due Diligence | High-risk Case Workflow | `CaseService.escalate()` | Integration Test |
| **SAR (BSA)** | Suspicious Activity Reporting | Alert Generation System | `StreamProcessor.emit_alert()` | E2E Scenario |
| **UBO (AMLD5)** | Ultimate Beneficial Ownership | Graph Relationship DB | `CustomerGraph.get_ubo()` | Data Validation |
| **WLF (OFAC)** | Watchlist Filtering | Fuzzy Matching Engine | `ScreeningService.match()` | Benchmark Set |

---

## Workflow Implementation

### KYC State Machine

```mermaid
stateDiagram-v2
    [*] --> Pending : Customer Created
    Pending --> Screening : Documents Uploaded
    
    state Screening {
        [*] --> ID_Check
        ID_Check --> Watchlist_Check
        Watchlist_Check --> Risk_Scoring
        Risk_Scoring --> [*]
    }
    
    Screening --> Verified : Low Risk
    Screening --> Review : High Risk / Alert
    
    Review --> Verified : Analyst Approval
    Review --> Rejected : Analyst Rejection
    
    Verified --> [*]
    Rejected --> [*]
```

### SAR Filing Process

```mermaid
sequenceDiagram
    participant Sys as System
    participant An as Analyst
    participant MLRO
    participant FIU as Fin. Intel Unit
    
    Sys->>An: Alert (Risk Score > 0.9)
    An->>An: Investigate Transaction
    An->>MLRO: Escalate for SAR
    MLRO->>MLRO: Review Evidence
    alt Confirm Suspicion
        MLRO->>Sys: Generate SAR Report
        Sys->>FIU: Submit SAR (XML/JSON)
        FIU-->>Sys: Acknowledge Receipt
    else False Positive
        MLRO->>Sys: Close Alert (No SAR)
    end
```

---

## Data Retention Policy

| Data Type | Retention Period | Rationale |
|-----------|------------------|-----------|
| KYC Documents | 5 years after closure | BSA Recordkeeping |
| Transaction Logs | 5 years | BSA Recordkeeping |
| SAR Filings | 5 years | FinCEN Requirement |
| Alert History | 5 years | Audit Trail |

---

## Disclaimer

This document is for technical implementation guidance only. It does not constitute legal advice.
