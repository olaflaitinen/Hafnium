package dev.hafnium.signals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Signals Service application.
 *
 * <p>
 * Ingests session and device signals, computes risk scores, and recommends
 * step-up actions.
 */
@SpringBootApplication
public class SignalsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignalsServiceApplication.class, args);
    }
}
