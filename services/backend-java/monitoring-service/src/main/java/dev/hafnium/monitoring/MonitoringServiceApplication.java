package dev.hafnium.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Monitoring Service Application.
 *
 * <p>
 * Provides transaction ingestion, rule engine processing, and alert generation
 * for the Hafnium
 * compliance platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.monitoring", "dev.hafnium.common" })
public class MonitoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringServiceApplication.class, args);
    }
}
