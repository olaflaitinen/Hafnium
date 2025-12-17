package dev.hafnium.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.identity.domain.KycWorkflow.WorkflowType;

/**
 * Request DTO for initiating KYC verification.
 */
public record KycInitiateRequest(
        @JsonProperty("workflow_type") WorkflowType workflowType,
        @JsonProperty("skip_screening") boolean skipScreening) {
}
