package dev.hafnium.facade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * API Facade Application.
 *
 * <p>
 * Provides a stable integration surface for frontend applications, aggregating
 * calls to
 * backend microservices.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "dev.hafnium.facade", "dev.hafnium.common" })
public class ApiFacadeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiFacadeApplication.class, args);
    }
}
