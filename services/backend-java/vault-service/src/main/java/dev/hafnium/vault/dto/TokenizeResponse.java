package dev.hafnium.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response DTO for tokenization.
 */
public record TokenizeResponse(@JsonProperty("tokens") Map<String, String> tokens) {
}
