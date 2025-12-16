package dev.hafnium.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Monitoring Service application.
 *
 * <p>
 * Handles transaction ingestion, rule-based alerting, and anomaly detection.
 */
@SpringBootApplication
public class MonitoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringServiceApplication.class, args);
    }
}
