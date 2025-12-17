package dev.hafnium.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for case data.
 */
public record CaseResponse(
                @JsonProperty("case_id") UUID caseId,
                @JsonProperty("title") String title,
                @JsonProperty("description") String description,
                @JsonProperty("case_type") String caseType,
                @JsonProperty("priority") String priority,
                @JsonProperty("status") String status,
                @JsonProperty("assigned_to") UUID assignedTo,
                @JsonProperty("customer_id") UUID customerId,
                @JsonProperty("alert_ids") List<UUID> alertIds,
                @JsonProperty("ai_summary") String aiSummary,
                @JsonProperty("resolution") String resolution,
                @JsonProperty("created_at") Instant createdAt,
                @JsonProperty("updated_at") Instant updatedAt,
                @JsonProperty("closed_at") Instant closedAt,
                @JsonProperty("due_date") Instant dueDate) {
}
