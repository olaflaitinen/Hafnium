package dev.hafnium.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hafnium.common.model.EventEnvelope;
import dev.hafnium.common.security.TenantContext;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * Publisher for domain events to Kafka topics.
 *
 * <p>
 * Wraps payloads in EventEnvelope with standard metadata.
 */
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes an event to the specified topic.
     *
     * @param topic         the Kafka topic
     * @param eventType     the event type identifier
     * @param schemaVersion the schema version
     * @param key           the partition key (typically entity ID)
     * @param payload       the event payload
     * @param <T>           the payload type
     * @return a future completing when the event is sent
     */
    public <T> CompletableFuture<SendResult<String, String>> publish(
            String topic, String eventType, String schemaVersion, String key, T payload) {

        TenantContext ctx = TenantContext.require();

        EventEnvelope<T> envelope = EventEnvelope.create(
                eventType,
                schemaVersion,
                ctx.getTenantId(),
                ctx.getUserId(),
                UUID.randomUUID(), // correlation ID
                payload);

        return publishEnvelope(topic, key, envelope);
    }

    /**
     * Publishes an event with explicit tenant context (for system events).
     *
     * @param topic         the Kafka topic
     * @param eventType     the event type identifier
     * @param schemaVersion the schema version
     * @param key           the partition key
     * @param tenantId      the tenant ID
     * @param actorId       the actor ID
     * @param correlationId the correlation ID
     * @param payload       the event payload
     * @param <T>           the payload type
     * @return a future completing when the event is sent
     */
    public <T> CompletableFuture<SendResult<String, String>> publishWithContext(
            String topic,
            String eventType,
            String schemaVersion,
            String key,
            UUID tenantId,
            UUID actorId,
            UUID correlationId,
            T payload) {

        EventEnvelope<T> envelope = EventEnvelope.create(eventType, schemaVersion, tenantId, actorId, correlationId,
                payload);

        return publishEnvelope(topic, key, envelope);
    }

    private <T> CompletableFuture<SendResult<String, String>> publishEnvelope(
            String topic, String key, EventEnvelope<T> envelope) {

        try {
            String json = objectMapper.writeValueAsString(envelope);

            log.info(
                    "Publishing event: topic={}, key={}, eventId={}, eventType={}",
                    topic,
                    key,
                    envelope.getEventId(),
                    envelope.getEventType());

            return kafkaTemplate.send(topic, key, json);

        } catch (Exception e) {
            log.error("Failed to serialize event: {}", envelope.getEventType(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
