package dev.hafnium.riskengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for risk scoring.
 */
public record RiskScoreRequest(
        @JsonProperty("entity_id") @NotNull UUID entityId,
        @JsonProperty("entity_type") @NotBlank String entityType,
        @JsonProperty("country") String country,
        @JsonProperty("transaction_count") int transactionCount,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("account_age_days") Integer accountAgeDays) {
}
