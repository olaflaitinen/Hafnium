package dev.hafnium.facade.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for the API facade.
 *
 * <p>
 * Provides liveness and readiness probes, as well as service discovery
 * information.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * Basic health check endpoint.
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "api-facade");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    /**
     * Service discovery endpoint.
     *
     * @return List of available services
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, String>> services() {
        Map<String, String> services = new HashMap<>();
        services.put("identity", "/api/v1/customers");
        services.put("screening", "/api/v1/screening");
        services.put("monitoring", "/api/v1/transactions");
        services.put("cases", "/api/v1/cases");
        services.put("vault", "/api/v1/vault");
        services.put("risk", "/api/v1/risk");
        services.put("signals", "/api/v1/signals");
        return ResponseEntity.ok(services);
    }
}
