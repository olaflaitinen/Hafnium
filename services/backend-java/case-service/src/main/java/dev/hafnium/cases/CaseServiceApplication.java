package dev.hafnium.cases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Case Service application.
 *
 * <p>
 * Manages investigation cases with state machine workflow and evidence
 * attachments.
 */
@SpringBootApplication
public class CaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}
