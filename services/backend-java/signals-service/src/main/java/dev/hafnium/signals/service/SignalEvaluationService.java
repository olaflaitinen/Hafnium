package dev.hafnium.signals.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.signals.dto.SessionSignalRequest;
import dev.hafnium.signals.dto.SignalResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating device and session risk signals.
 *
 * <p>
 * Analyzes session metadata to detect anomalies and determine if step-up
 * authentication is
 * required.
 */
@Service
public class SignalEvaluationService {

    private static final Logger LOG = LoggerFactory.getLogger(SignalEvaluationService.class);

    private static final double STEP_UP_THRESHOLD = 0.6;

    private final KafkaEventPublisher eventPublisher;

    public SignalEvaluationService(KafkaEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Evaluates session signals and determines if step-up is required.
     *
     * @param request The session signal request
     * @return The signal evaluation response
     */
    public SignalResponse evaluateSession(SessionSignalRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        List<SignalResponse.Signal> signals = new ArrayList<>();
        double totalRisk = 0.0;

        // Device fingerprint analysis
        if (request.deviceFingerprint() != null) {
            double deviceRisk = analyzeDeviceFingerprint(request.deviceFingerprint());
            if (deviceRisk > 0.2) {
                signals.add(new SignalResponse.Signal("device_risk", deviceRisk, "Device fingerprint anomaly"));
                totalRisk += deviceRisk * 0.3;
            }
        }

        // IP analysis
        if (request.ipAddress() != null) {
            double ipRisk = analyzeIpAddress(request.ipAddress());
            if (ipRisk > 0.2) {
                signals.add(new SignalResponse.Signal("ip_risk", ipRisk, "IP address risk signal"));
                totalRisk += ipRisk * 0.25;
            }
        }

        // Geolocation analysis
        if (request.geoLocation() != null) {
            double geoRisk = analyzeGeoLocation(request.geoLocation());
            if (geoRisk > 0.2) {
                signals.add(new SignalResponse.Signal("geo_risk", geoRisk, "Geographic anomaly detected"));
                totalRisk += geoRisk * 0.2;
            }
        }

        // Velocity check
        if (request.loginAttempts() != null && request.loginAttempts() > 3) {
            double velocityRisk = Math.min(1.0, request.loginAttempts() / 10.0);
            signals.add(new SignalResponse.Signal("velocity", velocityRisk, "Multiple login attempts"));
            totalRisk += velocityRisk * 0.25;
        }

        totalRisk = Math.min(1.0, totalRisk);
        boolean requiresStepUp = totalRisk >= STEP_UP_THRESHOLD;
        String[] stepUpMethods = requiresStepUp ? new String[] { "mfa_totp", "mfa_push" } : new String[] {};

        LOG.debug(
                "Evaluated session {} for tenant {}: risk={}, stepUp={}",
                request.sessionId(),
                tenantId,
                totalRisk,
                requiresStepUp);

        // Emit step-up event if required
        if (requiresStepUp) {
            eventPublisher.publish(
                    EventType.AUTH_STEPUP_REQUIRED,
                    tenantId,
                    actorId,
                    TenantContext.getOrCreateTraceId(),
                    Map.of(
                            "session_id", request.sessionId(),
                            "user_id", request.userId(),
                            "risk_score", totalRisk,
                            "methods", stepUpMethods));
        }

        return new SignalResponse(
                request.sessionId(), totalRisk, requiresStepUp, List.of(stepUpMethods), signals);
    }

    private double analyzeDeviceFingerprint(String fingerprint) {
        // Placeholder: check if device is known/trusted
        return 0.1;
    }

    private double analyzeIpAddress(String ipAddress) {
        // Check for VPN/Tor
        if (ipAddress.startsWith("10.") || ipAddress.startsWith("192.168.")) {
            return 0.0; // Internal IP
        }
        // Placeholder: would check IP reputation services
        return 0.2;
    }

    private double analyzeGeoLocation(Map<String, Object> geoLocation) {
        // Check for impossible travel
        String country = (String) geoLocation.get("country");
        if (country != null && List.of("XX", "YY", "ZZ").contains(country.toUpperCase())) {
            return 0.8; // High-risk geography
        }
        return 0.1;
    }
}
