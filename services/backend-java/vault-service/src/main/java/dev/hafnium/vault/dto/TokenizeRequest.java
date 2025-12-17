package dev.hafnium.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Request DTO for tokenization.
 */
public record TokenizeRequest(
                @JsonProperty("data") @NotEmpty Map<String, String> data,
                @JsonProperty("data_type") String dataType,
                @JsonProperty("retention_days") int retentionDays) {
}
