package dev.hafnium.cases.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.cases.domain.Case.CaseStatus;
import dev.hafnium.cases.domain.Case.Priority;
import java.util.UUID;

/**
 * Request DTO for updating a case.
 */
public record UpdateCaseRequest(
                @JsonProperty("status") CaseStatus status,
                @JsonProperty("priority") Priority priority,
                @JsonProperty("assigned_to") UUID assignedTo,
                @JsonProperty("resolution") String resolution) {
}
