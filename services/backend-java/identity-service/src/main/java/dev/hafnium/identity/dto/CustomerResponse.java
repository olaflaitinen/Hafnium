package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.Customer.CustomerType;
import dev.hafnium.identity.domain.Customer.RiskTier;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for customer data.
 */
public record CustomerResponse(
                @JsonProperty("customer_id") UUID customerId,
                @JsonProperty("external_id") String externalId,
                @JsonProperty("customer_type") CustomerType customerType,
                @JsonProperty("status") CustomerStatus status,
                @JsonProperty("risk_tier") RiskTier riskTier,
                @JsonProperty("metadata") Map<String, Object> metadata,
                @JsonProperty("created_at") Instant createdAt,
                @JsonProperty("updated_at") Instant updatedAt) {
}
