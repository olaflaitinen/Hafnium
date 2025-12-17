package dev.hafnium.common.model.event;

/**
 * Enum defining all event types used in the Hafnium platform.
 *
 * <p>
 * Event type names follow the convention: {domain}.{entity}.{action}.
 * These correspond to Kafka topic names defined in the AsyncAPI specification.
 */
public enum EventType {

    // Transaction events
    TRANSACTION_INGESTED("hf.txn.ingested.v1"),
    TRANSACTION_ENRICHED("hf.txn.enriched.v1"),
    TRANSACTION_SCORED("hf.txn.scored.v1"),

    // Alert events
    ALERT_RAISED("hf.alert.raised.v1"),
    ALERT_UPDATED("hf.alert.updated.v1"),

    // Case events
    CASE_CREATED("hf.case.created.v1"),
    CASE_UPDATED("hf.case.updated.v1"),

    // Customer events
    CUSTOMER_CREATED("hf.customer.created.v1"),
    CUSTOMER_VERIFIED("hf.customer.verified.v1"),

    // KYC events
    KYC_REQUESTED("hf.kyc.requested.v1"),
    KYC_COMPLETED("hf.kyc.completed.v1"),

    // Screening events
    SCREENING_REQUESTED("hf.screening.requested.v1"),
    SCREENING_COMPLETED("hf.screening.match.completed.v1"),

    // Risk events
    RISK_SCORED("hf.risk.scored.v1"),

    // Vault events
    VAULT_TOKEN_CREATED("hf.vault.token.created.v1"),

    // Auth events
    AUTH_STEPUP_REQUIRED("hf.auth.stepup.required.v1"),

    // Dead letter queue
    DEAD_LETTER("hf.dlq.v1");

    private final String topicName;

    EventType(String topicName) {
        this.topicName = topicName;
    }

    /**
     * Returns the Kafka topic name for this event type.
     *
     * @return The topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Returns the event type string for use in event envelopes.
     *
     * @return The event type identifier
     */
    public String getEventType() {
        return name();
    }
}
