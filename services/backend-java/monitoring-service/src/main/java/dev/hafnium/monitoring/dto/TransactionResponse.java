package dev.hafnium.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for transaction ingestion.
 */
public record TransactionResponse(
                @JsonProperty("txn_id") UUID txnId,
                @JsonProperty("external_txn_id") String externalTxnId,
                @JsonProperty("amount") BigDecimal amount,
                @JsonProperty("currency") String currency,
                @JsonProperty("txn_type") String txnType,
                @JsonProperty("risk_score") BigDecimal riskScore,
                @JsonProperty("alert_count") int alertCount,
                @JsonProperty("created_at") Instant createdAt) {
}
