package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.Customer.RiskTier;
import java.util.Map;

/**
 * Request DTO for updating a customer.
 */
public record UpdateCustomerRequest(
        @JsonProperty("status") CustomerStatus status,
        @JsonProperty("risk_tier") RiskTier riskTier,
        @JsonProperty("metadata") Map<String, Object> metadata) {
}
