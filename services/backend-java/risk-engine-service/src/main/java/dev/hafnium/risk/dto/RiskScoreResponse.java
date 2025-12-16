package dev.hafnium.risk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Response containing computed risk score.
 */
public record RiskScoreResponse(
        Double score,
        @JsonProperty("risk_level") String riskLevel,
        List<ReasonCode> reasons,
        @JsonProperty("policy_actions") List<PolicyAction> policyActions,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("computed_at") Instant computedAt,
        @JsonProperty("decision_id") String decisionId) {

    public record ReasonCode(String code, Double contribution, String description) {
    }

    public record PolicyAction(String action, Integer priority) {
    }
}
