package dev.hafnium.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to create a new customer.
 */
public record CreateCustomerRequest(
        @NotBlank(message = "External ID is required") String externalId,
        String customerType,
        String metadata) {
}
