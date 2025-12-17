package dev.hafnium.screening.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hafnium.screening.domain.ScreeningRequest.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for screening match operations.
 */
public record ScreeningMatchRequest(
        @JsonProperty("entity_id") @NotNull UUID entityId,
        @JsonProperty("entity_type") EntityType entityType,
        @JsonProperty("name") @NotBlank String name,
        @JsonProperty("date_of_birth") String dateOfBirth,
        @JsonProperty("country") String country,
        @JsonProperty("threshold") Double threshold) {
}
