package dev.hafnium.cases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Case Service Application.
 *
 * <p>
 * Provides case management, evidence handling, and investigation workflows for
 * the Hafnium
 * compliance platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.cases", "dev.hafnium.common" })
public class CaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}
