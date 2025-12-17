package dev.hafnium.signals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for signal evaluation.
 */
public record SignalResponse(
        @JsonProperty("session_id") UUID sessionId,
        @JsonProperty("risk_score") double riskScore,
        @JsonProperty("requires_step_up") boolean requiresStepUp,
        @JsonProperty("step_up_methods") List<String> stepUpMethods,
        @JsonProperty("signals") List<Signal> signals) {

    public record Signal(
            @JsonProperty("name") String name,
            @JsonProperty("score") double score,
            @JsonProperty("description") String description) {
    }
}
