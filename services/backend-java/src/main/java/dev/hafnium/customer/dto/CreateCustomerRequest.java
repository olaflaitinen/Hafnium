package dev.hafnium.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "External ID is required")
    @Size(max = 255, message = "External ID must not exceed 255 characters")
    private String externalId;

    @Builder.Default
    private String customerType = "INDIVIDUAL";

    private Object metadata;
}
