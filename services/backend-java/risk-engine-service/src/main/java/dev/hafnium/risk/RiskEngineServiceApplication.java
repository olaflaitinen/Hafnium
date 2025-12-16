package dev.hafnium.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Risk Engine Service application.
 *
 * <p>
 * Provides unified risk scoring with explainability, combining rules, ML
 * models, and security
 * signals.
 */
@SpringBootApplication
public class RiskEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskEngineServiceApplication.class, args);
    }
}
