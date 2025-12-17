package dev.hafnium.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.cases.domain.Case.CaseType;
import dev.hafnium.cases.domain.Case.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a case.
 */
public record CreateCaseRequest(
                @JsonProperty("title") @NotBlank String title,
                @JsonProperty("description") String description,
                @JsonProperty("case_type") @NotNull CaseType caseType,
                @JsonProperty("priority") Priority priority,
                @JsonProperty("assigned_to") UUID assignedTo,
                @JsonProperty("customer_id") UUID customerId,
                @JsonProperty("alert_ids") List<UUID> alertIds,
                @JsonProperty("due_date") Instant dueDate,
                @JsonProperty("metadata") Map<String, Object> metadata) {
}
