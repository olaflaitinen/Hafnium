package dev.hafnium.signals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Signals Service Application.
 *
 * <p>
 * Provides device/session risk signals and step-up authentication policies.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.signals", "dev.hafnium.common" })
public class SignalsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignalsServiceApplication.class, args);
    }
}
