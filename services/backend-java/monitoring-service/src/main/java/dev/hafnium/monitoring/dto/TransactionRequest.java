package dev.hafnium.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction ingestion request DTO.
 */
public record TransactionRequest(
        @NotBlank @JsonProperty("customer_id") String customerId,
        @NotBlank @JsonProperty("external_id") String externalId,
        @NotBlank @JsonProperty("transaction_type") String transactionType,
        @NotBlank String direction,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @JsonProperty("counterparty_name") String counterpartyName,
        @JsonProperty("counterparty_account") String counterpartyAccount,
        @JsonProperty("counterparty_country") String counterpartyCountry,
        String channel,
        @JsonProperty("transaction_timestamp") Instant transactionTimestamp) {
}
