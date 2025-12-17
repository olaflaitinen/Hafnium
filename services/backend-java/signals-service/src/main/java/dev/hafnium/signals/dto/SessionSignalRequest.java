package dev.hafnium.signals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for session signal evaluation.
 */
public record SessionSignalRequest(
        @JsonProperty("session_id") @NotNull UUID sessionId,
        @JsonProperty("user_id") @NotBlank String userId,
        @JsonProperty("device_fingerprint") String deviceFingerprint,
        @JsonProperty("ip_address") String ipAddress,
        @JsonProperty("user_agent") String userAgent,
        @JsonProperty("geo_location") Map<String, Object> geoLocation,
        @JsonProperty("login_attempts") Integer loginAttempts) {
}
