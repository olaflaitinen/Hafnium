package dev.hafnium.risk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request to compute a risk score.
 */
public record RiskScoreRequest(
        @NotBlank @JsonProperty("entity_type") String entityType,
        @NotBlank @JsonProperty("entity_id") String entityId,
        RiskContext context,
        Map<String, Double> features) {

    public record RiskContext(
            @JsonProperty("use_case") String useCase,
            Double amount,
            String currency) {
    }
}
