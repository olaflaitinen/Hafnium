package dev.hafnium.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Identity Service Application.
 *
 * <p>
 * Provides KYC orchestration, customer management, and document verification
 * capabilities for
 * the Hafnium compliance platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.identity", "dev.hafnium.common" })
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
