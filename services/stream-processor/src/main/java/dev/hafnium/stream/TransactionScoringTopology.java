package dev.hafnium.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Streams topology for transaction scoring.
 */
public class TransactionScoringTopology {

    private static final Logger log = LoggerFactory.getLogger(TransactionScoringTopology.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INPUT_TOPIC = "hf.txn.ingested.v1";
    private static final String OUTPUT_TOPIC = "hf.txn.scored.v1";

    public static void build(StreamsBuilder builder) {
        KStream<String, String> transactions = builder.stream(INPUT_TOPIC);

        transactions
                .mapValues(TransactionScoringTopology::enrichWithScore)
                .filter((key, value) -> value != null)
                .peek((key, value) -> log.debug("Scored transaction: key={}", key))
                .to(OUTPUT_TOPIC);
    }

    private static String enrichWithScore(String value) {
        try {
            JsonNode transaction = objectMapper.readTree(value);
            ObjectNode enriched = (ObjectNode) transaction.deepCopy();

            // Extract features for scoring
            double amount = transaction.path("amount").asDouble(0);
            String counterpartyCountry = transaction.path("counterparty_country").asText("");

            // Simple rule-based scoring (production uses AI service)
            double score = computeRuleScore(amount, counterpartyCountry);

            enriched.put("risk_score", score);
            enriched.put("risk_level", scoreToLevel(score));
            enriched.put("scored_at", System.currentTimeMillis());

            return objectMapper.writeValueAsString(enriched);
        } catch (Exception e) {
            log.error("Failed to process transaction", e);
            return null;
        }
    }

    private static double computeRuleScore(double amount, String country) {
        double score = 0.0;

        if (amount > 10000) {
            score += 0.2;
        }
        if (amount > 50000) {
            score += 0.3;
        }

        if ("IR".equals(country) || "KP".equals(country) || "SY".equals(country)) {
            score += 0.5;
        }

        return Math.min(score, 1.0);
    }

    private static String scoreToLevel(double score) {
        if (score >= 0.8)
            return "CRITICAL";
        if (score >= 0.6)
            return "HIGH";
        if (score >= 0.4)
            return "MEDIUM";
        return "LOW";
    }
}
