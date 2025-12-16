package dev.hafnium.screening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Screening Service application.
 *
 * <p>
 * Provides sanctions and PEP screening with fuzzy matching.
 */
@SpringBootApplication
public class ScreeningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreeningServiceApplication.class, args);
    }
}
