package dev.hafnium.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Case response DTO.
 */
public record CaseResponse(
        @JsonProperty("case_id") UUID caseId,
        @JsonProperty("case_number") String caseNumber,
        @JsonProperty("case_type") String caseType,
        String status,
        String priority,
        String subject,
        String description,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("assigned_to") UUID assignedTo,
        @JsonProperty("sla_due_at") Instant slaDueAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("closed_at") Instant closedAt) {
}
