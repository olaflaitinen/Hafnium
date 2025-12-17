package dev.hafnium.common.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Common event envelope for all Hafnium domain events.
 *
 * <p>
 * This envelope provides the required metadata fields for event-driven
 * communication across services. All events MUST be wrapped in this envelope
 * before publishing to Kafka.
 *
 * <p>
 * Required fields per AsyncAPI specification:
 * <ul>
 * <li>event_id - Unique identifier for this event instance</li>
 * <li>trace_id - Distributed tracing correlation ID</li>
 * <li>tenant_id - Multi-tenant isolation identifier</li>
 * <li>actor_id - Identity that triggered the event</li>
 * <li>occurred_at - Timestamp when the event occurred</li>
 * <li>schema_version - Version of the event schema</li>
 * </ul>
 *
 * @param <T> The payload type for this event
 */
public record EventEnvelope<T>(
        @JsonProperty("event_id") @NotNull UUID eventId,
        @JsonProperty("event_type") @NotBlank String eventType,
        @JsonProperty("trace_id") @NotNull UUID traceId,
        @JsonProperty("tenant_id") @NotNull UUID tenantId,
        @JsonProperty("actor_id") @NotBlank String actorId,
        @JsonProperty("occurred_at") @NotNull Instant occurredAt,
        @JsonProperty("schema_version") @NotBlank String schemaVersion,
        @JsonProperty("causation_id") UUID causationId,
        @JsonProperty("payload") @NotNull T payload) {

    /** Default schema version for new events. */
    public static final String DEFAULT_SCHEMA_VERSION = "1.0.0";

    /**
     * Creates a new event envelope with auto-generated event ID and current
     * timestamp.
     *
     * @param eventType The type name of the event
     * @param traceId   The distributed tracing correlation ID
     * @param tenantId  The tenant identifier
     * @param actorId   The actor that triggered this event
     * @param payload   The event payload
     * @param <T>       The payload type
     * @return A new EventEnvelope instance
     */
    public static <T> EventEnvelope<T> create(
            String eventType, UUID traceId, UUID tenantId, String actorId, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                traceId,
                tenantId,
                actorId,
                Instant.now(),
                DEFAULT_SCHEMA_VERSION,
                null,
                payload);
    }

    /**
     * Creates a new event envelope with causation tracking.
     *
     * @param eventType   The type name of the event
     * @param traceId     The distributed tracing correlation ID
     * @param tenantId    The tenant identifier
     * @param actorId     The actor that triggered this event
     * @param causationId The ID of the event that caused this event
     * @param payload     The event payload
     * @param <T>         The payload type
     * @return A new EventEnvelope instance
     */
    public static <T> EventEnvelope<T> createWithCausation(
            String eventType,
            UUID traceId,
            UUID tenantId,
            String actorId,
            UUID causationId,
            T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                traceId,
                tenantId,
                actorId,
                Instant.now(),
                DEFAULT_SCHEMA_VERSION,
                causationId,
                payload);
    }
}
