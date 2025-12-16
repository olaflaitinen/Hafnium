package dev.hafnium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hafnium Backend Application.
 *
 * <p>
 * Main entry point for the Spring Boot backend service providing REST APIs for
 * the Hafnium
 * platform.
 */
@SpringBootApplication
public class HafniumApplication {

    public static void main(String[] args) {
        SpringApplication.run(HafniumApplication.class, args);
    }
}
