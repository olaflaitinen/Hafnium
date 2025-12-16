package dev.hafnium.screening.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Screening request DTO.
 */
public record ScreeningRequest(
        @NotBlank String name,
        @JsonProperty("entity_type") String entityType,
        @JsonProperty("date_of_birth") String dateOfBirth,
        String country) {
}
