package dev.hafnium.riskengine.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.riskengine.dto.RiskScoreRequest;
import dev.hafnium.riskengine.dto.RiskScoreResponse;
import dev.hafnium.riskengine.dto.RiskScoreResponse.RiskFactor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for unified risk scoring.
 *
 * <p>
 * Combines rule-based scoring with ML model predictions. Falls back to
 * rules-only when ML is
 * unavailable.
 */
@Service
public class RiskScoringService {

    private static final Logger LOG = LoggerFactory.getLogger(RiskScoringService.class);

    private final MlInferenceClient mlClient;
    private final KafkaEventPublisher eventPublisher;
    private final Counter riskScoreCounter;
    private final Timer scoringTimer;

    public RiskScoringService(
            MlInferenceClient mlClient,
            KafkaEventPublisher eventPublisher,
            MeterRegistry meterRegistry) {
        this.mlClient = mlClient;
        this.eventPublisher = eventPublisher;

        this.riskScoreCounter = Counter.builder("hafnium.risk.scores")
                .description("Total risk scores calculated")
                .register(meterRegistry);

        this.scoringTimer = Timer.builder("hafnium.risk.scoring.duration")
                .description("Risk scoring duration")
                .register(meterRegistry);
    }

    /**
     * Calculates unified risk score for an entity.
     *
     * @param request The risk score request
     * @return The risk score response
     */
    public RiskScoreResponse calculateRiskScore(RiskScoreRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        return scoringTimer.record(
                () -> {
                    List<RiskFactor> factors = new ArrayList<>();
                    BigDecimal ruleScore = calculateRuleBasedScore(request, factors);

                    BigDecimal mlScore = BigDecimal.ZERO;
                    String modelId = null;
                    String modelVersion = null;

                    // Attempt ML scoring
                    try {
                        MlInferenceClient.MlPrediction prediction = mlClient.predict(request);
                        if (prediction != null) {
                            mlScore = prediction.score();
                            modelId = prediction.modelId();
                            modelVersion = prediction.modelVersion();
                            factors.add(new RiskFactor("ml_model_prediction", mlScore.doubleValue(),
                                    "ML model risk assessment"));
                        }
                    } catch (Exception e) {
                        LOG.warn("ML inference failed, using rules-only: {}", e.getMessage());
                    }

                    // Combine scores (weighted average if ML available)
                    BigDecimal finalScore;
                    if (mlScore.compareTo(BigDecimal.ZERO) > 0) {
                        // 60% ML, 40% rules when ML available
                        finalScore = mlScore.multiply(new BigDecimal("0.6"))
                                .add(ruleScore.multiply(new BigDecimal("0.4")))
                                .setScale(4, RoundingMode.HALF_UP);
                    } else {
                        finalScore = ruleScore;
                    }

                    String riskLevel = determineRiskLevel(finalScore);

                    riskScoreCounter.increment();

                    // Emit event
                    eventPublisher.publish(
                            EventType.RISK_SCORED,
                            tenantId,
                            actorId,
                            TenantContext.getOrCreateTraceId(),
                            Map.of(
                                    "entity_id", request.entityId(),
                                    "entity_type", request.entityType(),
                                    "score", finalScore,
                                    "risk_level", riskLevel,
                                    "model_id", modelId != null ? modelId : "rules_only"));

                    return new RiskScoreResponse(
                            request.entityId(),
                            request.entityType(),
                            finalScore,
                            riskLevel,
                            factors,
                            modelId,
                            modelVersion);
                });
    }

    private BigDecimal calculateRuleBasedScore(RiskScoreRequest request, List<RiskFactor> factors) {
        BigDecimal score = BigDecimal.ZERO;

        // Country risk
        if (request.country() != null) {
            double countryRisk = getCountryRiskScore(request.country());
            if (countryRisk > 0.3) {
                factors.add(new RiskFactor("country_risk", countryRisk, "Geographic risk factor"));
                score = score.add(BigDecimal.valueOf(countryRisk * 0.3));
            }
        }

        // Transaction velocity
        if (request.transactionCount() > 10) {
            double velocityRisk = Math.min(1.0, request.transactionCount() / 50.0);
            factors.add(new RiskFactor("velocity", velocityRisk, "High transaction velocity"));
            score = score.add(BigDecimal.valueOf(velocityRisk * 0.2));
        }

        // Amount risk
        if (request.totalAmount() != null && request.totalAmount().compareTo(new BigDecimal("50000")) > 0) {
            double amountRisk = Math.min(1.0,
                    request.totalAmount().divide(new BigDecimal("100000"), RoundingMode.HALF_UP).doubleValue());
            factors.add(new RiskFactor("high_value", amountRisk, "High transaction amounts"));
            score = score.add(BigDecimal.valueOf(amountRisk * 0.25));
        }

        // Customer age risk (new customers)
        if (request.accountAgeDays() != null && request.accountAgeDays() < 30) {
            double ageRisk = 1.0 - (request.accountAgeDays() / 30.0);
            factors.add(new RiskFactor("new_account", ageRisk, "Recently opened account"));
            score = score.add(BigDecimal.valueOf(ageRisk * 0.15));
        }

        return score.min(BigDecimal.ONE).setScale(4, RoundingMode.HALF_UP);
    }

    private double getCountryRiskScore(String country) {
        // Sample risk scores for demonstration
        return switch (country.toUpperCase()) {
            case "XX", "YY", "ZZ" -> 0.9; // Sample high-risk
            default -> 0.1;
        };
    }

    private String determineRiskLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.75")) >= 0)
            return "critical";
        if (score.compareTo(new BigDecimal("0.5")) >= 0)
            return "high";
        if (score.compareTo(new BigDecimal("0.25")) >= 0)
            return "medium";
        return "low";
    }
}
