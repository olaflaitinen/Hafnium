package dev.hafnium.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenizeRequest(
        @NotBlank @JsonProperty("data_type") String dataType,
        @NotBlank String value) {
}
