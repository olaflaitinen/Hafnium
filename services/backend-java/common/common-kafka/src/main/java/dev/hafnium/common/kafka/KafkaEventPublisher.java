package dev.hafnium.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hafnium.common.model.event.EventEnvelope;
import dev.hafnium.common.model.event.EventType;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

/**
 * Generic event publisher for Kafka with envelope wrapping.
 *
 * <p>
 * This publisher wraps all events in the standard {@link EventEnvelope} and
 * publishes to the
 * appropriate Kafka topic based on the event type. It automatically adds
 * headers for routing and
 * tracing.
 */
@Component
public class KafkaEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private static final String HEADER_EVENT_TYPE = "event_type";
    private static final String HEADER_TENANT_ID = "tenant_id";
    private static final String HEADER_TRACE_ID = "trace_id";
    private static final String HEADER_SCHEMA_VERSION = "schema_version";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes an event to Kafka with full envelope wrapping.
     *
     * @param eventType The type of event being published
     * @param tenantId  The tenant identifier
     * @param actorId   The actor that triggered this event
     * @param traceId   The distributed trace ID
     * @param payload   The event payload
     * @param <T>       The payload type
     * @return CompletableFuture with the send result
     */
    public <T> CompletableFuture<SendResult<String, String>> publish(
            EventType eventType, UUID tenantId, String actorId, UUID traceId, T payload) {

        EventEnvelope<T> envelope = EventEnvelope.create(eventType.getEventType(), traceId, tenantId, actorId, payload);

        return publishEnvelope(eventType.getTopicName(), envelope);
    }

    /**
     * Publishes an event with causation tracking.
     *
     * @param eventType   The type of event being published
     * @param tenantId    The tenant identifier
     * @param actorId     The actor that triggered this event
     * @param traceId     The distributed trace ID
     * @param causationId The ID of the event that caused this event
     * @param payload     The event payload
     * @param <T>         The payload type
     * @return CompletableFuture with the send result
     */
    public <T> CompletableFuture<SendResult<String, String>> publishWithCausation(
            EventType eventType,
            UUID tenantId,
            String actorId,
            UUID traceId,
            UUID causationId,
            T payload) {

        EventEnvelope<T> envelope = EventEnvelope.createWithCausation(
                eventType.getEventType(), traceId, tenantId, actorId, causationId, payload);

        return publishEnvelope(eventType.getTopicName(), envelope);
    }

    /**
     * Publishes a pre-wrapped envelope to a specific topic.
     *
     * @param topic    The Kafka topic name
     * @param envelope The event envelope
     * @param <T>      The payload type
     * @return CompletableFuture with the send result
     */
    public <T> CompletableFuture<SendResult<String, String>> publishEnvelope(
            String topic, EventEnvelope<T> envelope) {

        try {
            String json = objectMapper.writeValueAsString(envelope);

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, envelope.tenantId().toString(), json);

            // Add headers for routing and observability
            record.headers().add(new RecordHeader(HEADER_EVENT_TYPE,
                    envelope.eventType().getBytes()));
            record.headers().add(new RecordHeader(HEADER_TENANT_ID,
                    envelope.tenantId().toString().getBytes()));
            record.headers().add(new RecordHeader(HEADER_TRACE_ID,
                    envelope.traceId().toString().getBytes()));
            record.headers().add(new RecordHeader(HEADER_SCHEMA_VERSION,
                    envelope.schemaVersion().getBytes()));

            LOG.debug(
                    "Publishing event {} to topic {} for tenant {}",
                    envelope.eventId(),
                    topic,
                    envelope.tenantId());

            return kafkaTemplate.send(record);

        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize event envelope", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publishes to the dead letter queue.
     *
     * @param originalTopic The original topic that failed
     * @param originalEvent The original event that failed
     * @param errorMessage  The error message
     * @param errorType     The type of error
     * @param retryCount    The number of retries attempted
     * @param tenantId      The tenant identifier
     * @param traceId       The trace ID
     * @return CompletableFuture with the send result
     */
    public CompletableFuture<SendResult<String, String>> publishToDeadLetter(
            String originalTopic,
            Object originalEvent,
            String errorMessage,
            String errorType,
            int retryCount,
            UUID tenantId,
            UUID traceId) {

        DeadLetterPayload dlqPayload = new DeadLetterPayload(originalTopic, originalEvent, errorMessage, errorType,
                retryCount);

        return publish(EventType.DEAD_LETTER, tenantId, "system", traceId, dlqPayload);
    }

    /** Payload for dead letter queue events. */
    public record DeadLetterPayload(
            String originalTopic,
            Object originalEvent,
            String errorMessage,
            String errorType,
            int retryCount) {
    }
}
