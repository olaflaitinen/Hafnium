package dev.hafnium.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for alert data.
 */
public record AlertResponse(
        @JsonProperty("alert_id") UUID alertId,
        @JsonProperty("rule_id") String ruleId,
        @JsonProperty("rule_name") String ruleName,
        @JsonProperty("severity") String severity,
        @JsonProperty("score") BigDecimal score,
        @JsonProperty("txn_id") UUID txnId,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("status") String status,
        @JsonProperty("explanation") String explanation,
        @JsonProperty("triggered_conditions") Map<String, Object> triggeredConditions,
        @JsonProperty("case_id") UUID caseId,
        @JsonProperty("created_at") Instant createdAt) {
}
