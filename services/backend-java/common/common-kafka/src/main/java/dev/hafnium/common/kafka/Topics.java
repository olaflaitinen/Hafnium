package dev.hafnium.common.kafka;

/**
 * Constants for Kafka topic names.
 *
 * <p>
 * Topic naming convention: hf.{domain}.{event}.v{version}
 */
public final class Topics {

    private Topics() {
    }

    // Transaction domain
    public static final String TXN_INGESTED = "hf.txn.ingested.v1";
    public static final String TXN_ENRICHED = "hf.txn.enriched.v1";
    public static final String TXN_SCORED = "hf.txn.scored.v1";

    // Alert domain
    public static final String ALERT_RAISED = "hf.alert.raised.v1";
    public static final String ALERT_UPDATED = "hf.alert.updated.v1";

    // Case domain
    public static final String CASE_CREATED = "hf.case.created.v1";
    public static final String CASE_UPDATED = "hf.case.updated.v1";

    // Customer/Identity domain
    public static final String CUSTOMER_VERIFIED = "hf.customer.verified.v1";
    public static final String KYC_REQUESTED = "hf.kyc.requested.v1";
    public static final String KYC_COMPLETED = "hf.kyc.completed.v1";

    // Screening domain
    public static final String SCREENING_COMPLETED = "hf.screening.match.completed.v1";

    // Risk domain
    public static final String RISK_SCORED = "hf.risk.scored.v1";

    // Auth/Signals domain
    public static final String AUTH_STEPUP_REQUIRED = "hf.auth.stepup.required.v1";

    // Vault domain
    public static final String VAULT_TOKEN_CREATED = "hf.vault.token.created.v1";

    // Dead letter queue
    public static final String DLQ = "hf.dlq.v1";
}
