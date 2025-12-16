package dev.hafnium.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Streams topology for alert enrichment.
 */
public class AlertEnrichmentTopology {

    private static final Logger log = LoggerFactory.getLogger(AlertEnrichmentTopology.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INPUT_TOPIC = "hf.alert.raised.v1";
    private static final String OUTPUT_TOPIC = "hf.alert.enriched.v1";

    public static void build(StreamsBuilder builder) {
        KStream<String, String> alerts = builder.stream(INPUT_TOPIC);

        alerts
                .mapValues(AlertEnrichmentTopology::enrichAlert)
                .filter((key, value) -> value != null)
                .peek((key, value) -> log.debug("Enriched alert: key={}", key))
                .to(OUTPUT_TOPIC);
    }

    private static String enrichAlert(String value) {
        try {
            JsonNode alert = objectMapper.readTree(value);
            ObjectNode enriched = (ObjectNode) alert.deepCopy();

            // Add enrichment metadata
            enriched.put("enriched_at", System.currentTimeMillis());
            enriched.put("enrichment_version", "1.0.0");

            // Add priority based on severity
            String severity = alert.path("severity").asText("LOW");
            enriched.put("priority", severityToPriority(severity));

            // Add SLA deadline
            long slaDays = getSlaForSeverity(severity);
            enriched.put("sla_deadline_ms", System.currentTimeMillis() + slaDays * 86400000L);

            return objectMapper.writeValueAsString(enriched);
        } catch (Exception e) {
            log.error("Failed to enrich alert", e);
            return null;
        }
    }

    private static int severityToPriority(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            default -> 4;
        };
    }

    private static int getSlaForSeverity(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 3;
            case "MEDIUM" -> 7;
            default -> 14;
        };
    }
}
