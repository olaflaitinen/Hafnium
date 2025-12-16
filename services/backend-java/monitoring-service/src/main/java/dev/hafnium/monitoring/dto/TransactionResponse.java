package dev.hafnium.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Transaction ingestion response DTO.
 */
public record TransactionResponse(
        @JsonProperty("transaction_id") UUID transactionId,
        @JsonProperty("external_id") String externalId,
        @JsonProperty("risk_score") Double riskScore,
        @JsonProperty("alert_count") int alertCount) {
}
