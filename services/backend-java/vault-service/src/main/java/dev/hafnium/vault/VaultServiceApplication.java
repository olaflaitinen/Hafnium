package dev.hafnium.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Vault Service Application.
 *
 * <p>
 * Provides PII tokenization boundary for data protection in the Hafnium
 * compliance platform.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.vault", "dev.hafnium.common" })
public class VaultServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultServiceApplication.class, args);
    }
}
