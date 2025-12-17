package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.identity.domain.KycWorkflow.WorkflowStep;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for KYC workflow data.
 */
public record KycWorkflowResponse(
        @JsonProperty("workflow_id") UUID workflowId,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("workflow_type") String workflowType,
        @JsonProperty("status") String status,
        @JsonProperty("current_step") String currentStep,
        @JsonProperty("steps") List<WorkflowStep> steps,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("completed_at") Instant completedAt) {
}
