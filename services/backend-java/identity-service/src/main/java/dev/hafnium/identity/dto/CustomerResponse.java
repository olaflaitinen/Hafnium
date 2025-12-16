package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Customer response DTO.
 */
public record CustomerResponse(
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("external_id") String externalId,
        @JsonProperty("customer_type") String customerType,
        String status,
        @JsonProperty("risk_tier") String riskTier,
        String metadata,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {
}
