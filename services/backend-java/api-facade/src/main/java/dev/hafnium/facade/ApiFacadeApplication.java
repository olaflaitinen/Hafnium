package dev.hafnium.facade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Facade application.
 *
 * <p>
 * Provides a stable integration surface for frontend with consistent auth,
 * correlation, and
 * error mapping.
 */
@SpringBootApplication
public class ApiFacadeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiFacadeApplication.class, args);
    }
}
