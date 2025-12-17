package dev.hafnium.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.monitoring.domain.Transaction.Channel;
import dev.hafnium.monitoring.domain.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for transaction ingestion.
 */
public record TransactionRequest(
                @JsonProperty("customer_id") UUID customerId,
                @JsonProperty("external_txn_id") String externalTxnId,
                @JsonProperty("amount") @NotNull BigDecimal amount,
                @JsonProperty("currency") @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
                @JsonProperty("txn_type") @NotNull TransactionType txnType,
                @JsonProperty("txn_timestamp") @NotNull Instant txnTimestamp,
                @JsonProperty("counterparty_id") String counterpartyId,
                @JsonProperty("counterparty_name") String counterpartyName,
                @JsonProperty("channel") Channel channel,
                @JsonProperty("geo_data") Map<String, Object> geoData,
                @JsonProperty("metadata") Map<String, Object> metadata) {
}
