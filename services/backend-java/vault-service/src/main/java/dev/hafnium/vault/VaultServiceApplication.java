package dev.hafnium.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vault Service application.
 *
 * <p>
 * Provides PII tokenization with deterministic HMAC and AES-GCM encryption.
 */
@SpringBootApplication
public class VaultServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultServiceApplication.class, args);
    }
}
