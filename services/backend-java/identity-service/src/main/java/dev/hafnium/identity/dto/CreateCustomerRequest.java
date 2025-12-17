package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.identity.domain.Customer.CustomerType;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request DTO for creating a new customer.
 */
public record CreateCustomerRequest(
                @JsonProperty("external_id") @NotBlank String externalId,
                @JsonProperty("customer_type") CustomerType customerType,
                @JsonProperty("metadata") Map<String, Object> metadata) {
}
