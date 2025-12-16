package dev.hafnium.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetokenizeResponse(
        String value,
        @JsonProperty("data_type") String dataType) {
}
