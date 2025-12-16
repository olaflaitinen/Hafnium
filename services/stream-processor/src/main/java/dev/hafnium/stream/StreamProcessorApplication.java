package dev.hafnium.stream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Hafnium Stream Processor Application.
 * Processes transaction and alert events in real-time.
 */
public class StreamProcessorApplication {

    private static final Logger log = LoggerFactory.getLogger(StreamProcessorApplication.class);

    public static void main(String[] args) {
        Properties props = createStreamProperties();
        Topology topology = buildTopology();

        log.info("Starting Hafnium Stream Processor");
        log.info("Topology: {}", topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, props);

        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down stream processor");
            streams.close();
            latch.countDown();
        }));

        try {
            streams.start();
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Stream processor interrupted", e);
        }
    }

    private static Properties createStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,
                getEnv("APPLICATION_ID", "hafnium-stream-processor"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                getEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG,
                StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG,
                Integer.parseInt(getEnv("NUM_THREADS", "2")));
        return props;
    }

    private static Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // Transaction scoring topology
        TransactionScoringTopology.build(builder);

        // Alert enrichment topology
        AlertEnrichmentTopology.build(builder);

        return builder.build();
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
