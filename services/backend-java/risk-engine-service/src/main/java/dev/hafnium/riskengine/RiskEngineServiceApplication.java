package dev.hafnium.riskengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Risk Engine Service Application.
 *
 * <p>
 * Provides unified risk scoring combining rule-based and ML-based risk
 * assessment.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.riskengine", "dev.hafnium.common" })
public class RiskEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskEngineServiceApplication.class, args);
    }
}
