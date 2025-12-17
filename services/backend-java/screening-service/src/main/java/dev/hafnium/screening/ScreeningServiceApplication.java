package dev.hafnium.screening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Screening Service Application.
 *
 * <p>
 * Provides sanctions and PEP screening capabilities with fuzzy matching for the
 * Hafnium
 * compliance platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.screening", "dev.hafnium.common" })
public class ScreeningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreeningServiceApplication.class, args);
    }
}
