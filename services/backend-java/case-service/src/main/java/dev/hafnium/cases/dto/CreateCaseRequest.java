package dev.hafnium.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Create case request DTO.
 */
public record CreateCaseRequest(
        @NotBlank @JsonProperty("case_type") String caseType,
        @NotBlank String priority,
        @NotBlank String subject,
        String description,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("alert_ids") java.util.List<String> alertIds) {
}
