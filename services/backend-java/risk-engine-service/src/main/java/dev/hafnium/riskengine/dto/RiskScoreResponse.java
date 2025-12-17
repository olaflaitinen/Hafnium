package dev.hafnium.riskengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for risk scoring.
 */
public record RiskScoreResponse(
        @JsonProperty("entity_id") UUID entityId,
        @JsonProperty("entity_type") String entityType,
        @JsonProperty("score") BigDecimal score,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("factors") List<RiskFactor> factors,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion) {

    public record RiskFactor(
            @JsonProperty("name") String name,
            @JsonProperty("contribution") double contribution,
            @JsonProperty("description") String description) {
    }
}
