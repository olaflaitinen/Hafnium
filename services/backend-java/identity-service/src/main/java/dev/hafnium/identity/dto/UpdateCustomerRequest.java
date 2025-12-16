package dev.hafnium.identity.dto;

/**
 * Request to update a customer.
 */
public record UpdateCustomerRequest(String status, String riskTier, String metadata) {
}
