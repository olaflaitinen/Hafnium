package dev.hafnium.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Standard event envelope for all domain events.
 *
 * <p>
 * All events published to Kafka must use this envelope structure to ensure
 * consistent metadata
 * propagation across services.
 */
@Value
@Builder
@Jacksonized
public class EventEnvelope<T> {

    @NotNull
    @JsonProperty("event_id")
    UUID eventId;

    @NotBlank
    @JsonProperty("event_type")
    String eventType;

    @NotBlank
    @JsonProperty("schema_version")
    String schemaVersion;

    @NotNull
    @JsonProperty("tenant_id")
    UUID tenantId;

    @JsonProperty("actor_id")
    UUID actorId;

    @JsonProperty("correlation_id")
    UUID correlationId;

    @JsonProperty("causation_id")
    UUID causationId;

    @NotNull
    @JsonProperty("occurred_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant occurredAt;

    @NotNull
    T payload;

    /**
     * Creates a new event envelope with auto-generated event ID and timestamp.
     *
     * @param eventType     the event type identifier
     * @param schemaVersion the schema version (e.g., "1.0.0")
     * @param tenantId      the tenant identifier
     * @param actorId       the actor who triggered the event
     * @param correlationId the correlation ID for request tracing
     * @param payload       the event payload
     * @param <T>           the payload type
     * @return a new event envelope
     */
    public static <T> EventEnvelope<T> create(
            String eventType,
            String schemaVersion,
            UUID tenantId,
            UUID actorId,
            UUID correlationId,
            T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .schemaVersion(schemaVersion)
                .tenantId(tenantId)
                .actorId(actorId)
                .correlationId(correlationId)
                .occurredAt(Instant.now())
                .payload(payload)
                .build();
    }
}
