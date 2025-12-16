package dev.hafnium.customer.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private UUID customerId;
    private String externalId;
    private String customerType;
    private String status;
    private String riskTier;
    private Object metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
