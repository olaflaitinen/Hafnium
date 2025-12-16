package dev.hafnium.vault.dto;

import jakarta.validation.constraints.NotBlank;

public record DetokenizeRequest(
        @NotBlank String token,
        @NotBlank String reason) {
}
