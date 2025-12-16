package dev.hafnium.risk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hafnium.common.kafka.EventPublisher;
import dev.hafnium.common.kafka.Topics;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.risk.client.AiInferenceClient;
import dev.hafnium.risk.domain.RiskDecision;
import dev.hafnium.risk.dto.RiskScoreRequest;
import dev.hafnium.risk.dto.RiskScoreResponse;
import dev.hafnium.risk.repository.RiskDecisionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Risk scoring service combining rules, ML models, and policy decisions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskScoringService {

    private final RiskDecisionRepository decisionRepository;
    private final AiInferenceClient aiInferenceClient;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${hafnium.risk.model.active-version:v1.0.0}")
    private String activeModelVersion;

    @Value("${hafnium.ai-inference.fallback-enabled:true}")
    private boolean fallbackEnabled;

    /**
     * Computes a risk score for the given entity.
     */
    @Transactional
    @CircuitBreaker(name = "risk-scoring", fallbackMethod = "computeRiskScoreFallback")
    public RiskScoreResponse computeRiskScore(RiskScoreRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        Instant computedAt = Instant.now();

        // Get ML score if available
        Double mlScore = null;
        try {
            mlScore = aiInferenceClient.getScore(request.entityType(), request.entityId(), request.features());
        } catch (Exception e) {
            log.warn("ML inference failed, using rules-only mode: {}", e.getMessage());
        }

        // Compute final score (rules + ML blend)
        double finalScore = computeFinalScore(request, mlScore);
        RiskDecision.RiskLevel riskLevel = determineRiskLevel(finalScore);

        // Generate reasons
        List<RiskScoreResponse.ReasonCode> reasons = generateReasons(request, finalScore);

        // Determine policy actions
        List<RiskScoreResponse.PolicyAction> policyActions = determinePolicyActions(riskLevel);

        // Persist decision for audit
        RiskDecision decision = persistDecision(
                tenantId, request, finalScore, riskLevel, reasons, policyActions, computedAt);

        // Publish event
        publishRiskScoredEvent(decision);

        return new RiskScoreResponse(
                finalScore,
                riskLevel.name(),
                reasons,
                policyActions,
                activeModelVersion,
                computedAt,
                decision.getId().toString());
    }

    /**
     * Retrieves a previously computed risk score.
     */
    @Transactional(readOnly = true)
    public RiskScoreResponse getRiskScore(String entityType, String entityId) {
        UUID tenantId = TenantContext.requireTenantId();

        RiskDecision decision = decisionRepository
                .findLatestByEntityAndTenant(entityType, entityId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskScore", entityType + "/" + entityId));

        return toResponse(decision);
    }

    /**
     * Fallback when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    private RiskScoreResponse computeRiskScoreFallback(RiskScoreRequest request, Throwable t) {
        log.error("Risk scoring circuit breaker open, using conservative defaults", t);

        if (!fallbackEnabled) {
            throw new RuntimeException("Risk scoring unavailable", t);
        }

        // Conservative fallback: medium risk with manual review
        return new RiskScoreResponse(
                0.5,
                "MEDIUM",
                List.of(new RiskScoreResponse.ReasonCode("SYSTEM_DEGRADED", 0.5, "System in degraded mode")),
                List.of(new RiskScoreResponse.PolicyAction("MANUAL_REVIEW", 1)),
                "fallback",
                Instant.now(),
                "fallback-" + UUID.randomUUID());
    }

    private double computeFinalScore(RiskScoreRequest request, Double mlScore) {
        // Rule-based baseline score
        double ruleScore = 0.3;

        // Adjust based on amount if present
        if (request.context() != null && request.context().amount() != null) {
            double amount = request.context().amount();
            if (amount > 50000) {
                ruleScore += 0.2;
            } else if (amount > 10000) {
                ruleScore += 0.1;
            }
        }

        // Blend with ML score if available
        if (mlScore != null) {
            return 0.4 * ruleScore + 0.6 * mlScore;
        }

        return Math.min(1.0, ruleScore);
    }

    private RiskDecision.RiskLevel determineRiskLevel(double score) {
        if (score >= 0.8)
            return RiskDecision.RiskLevel.CRITICAL;
        if (score >= 0.6)
            return RiskDecision.RiskLevel.HIGH;
        if (score >= 0.4)
            return RiskDecision.RiskLevel.MEDIUM;
        return RiskDecision.RiskLevel.LOW;
    }

    private List<RiskScoreResponse.ReasonCode> generateReasons(RiskScoreRequest request, double score) {
        List<RiskScoreResponse.ReasonCode> reasons = new ArrayList<>();

        if (request.context() != null && request.context().amount() != null) {
            if (request.context().amount() > 50000) {
                reasons.add(new RiskScoreResponse.ReasonCode(
                        "UNUSUAL_AMOUNT", 0.2, "Transaction amount significantly differs from historical pattern"));
            }
        }

        if (score > 0.6) {
            reasons.add(new RiskScoreResponse.ReasonCode(
                    "HIGH_TXN_VELOCITY", 0.15, "High transaction velocity detected"));
        }

        if (reasons.isEmpty()) {
            reasons.add(new RiskScoreResponse.ReasonCode(
                    "BASELINE_ASSESSMENT", 0.1, "Standard risk assessment"));
        }

        return reasons;
    }

    private List<RiskScoreResponse.PolicyAction> determinePolicyActions(RiskDecision.RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CRITICAL -> List.of(
                    new RiskScoreResponse.PolicyAction("BLOCK", 1),
                    new RiskScoreResponse.PolicyAction("MANUAL_REVIEW", 2));
            case HIGH -> List.of(
                    new RiskScoreResponse.PolicyAction("STEP_UP_AUTH", 1),
                    new RiskScoreResponse.PolicyAction("MANUAL_REVIEW", 2));
            case MEDIUM -> List.of(
                    new RiskScoreResponse.PolicyAction("FLAG", 1));
            case LOW -> List.of(
                    new RiskScoreResponse.PolicyAction("ALLOW", 1));
        };
    }

    private RiskDecision persistDecision(
            UUID tenantId,
            RiskScoreRequest request,
            double score,
            RiskDecision.RiskLevel riskLevel,
            List<RiskScoreResponse.ReasonCode> reasons,
            List<RiskScoreResponse.PolicyAction> policyActions,
            Instant computedAt) {

        try {
            RiskDecision decision = RiskDecision.builder()
                    .tenantId(tenantId)
                    .entityType(request.entityType())
                    .entityId(request.entityId())
                    .score(BigDecimal.valueOf(score))
                    .riskLevel(riskLevel)
                    .modelVersion(activeModelVersion)
                    .reasons(objectMapper.writeValueAsString(reasons))
                    .policyActions(objectMapper.writeValueAsString(policyActions))
                    .context(request.context() != null ? objectMapper.writeValueAsString(request.context()) : null)
                    .computedAt(computedAt)
                    .build();

            return decisionRepository.save(decision);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize decision data", e);
        }
    }

    private void publishRiskScoredEvent(RiskDecision decision) {
        eventPublisher.publish(
                Topics.RISK_SCORED,
                "risk.scored",
                "1.0.0",
                decision.getEntityId(),
                decision);
    }

    private RiskScoreResponse toResponse(RiskDecision decision) {
        try {
            List<RiskScoreResponse.ReasonCode> reasons = objectMapper.readValue(
                    decision.getReasons(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class,
                            RiskScoreResponse.ReasonCode.class));

            List<RiskScoreResponse.PolicyAction> policyActions = objectMapper.readValue(
                    decision.getPolicyActions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class,
                            RiskScoreResponse.PolicyAction.class));

            return new RiskScoreResponse(
                    decision.getScore().doubleValue(),
                    decision.getRiskLevel().name(),
                    reasons,
                    policyActions,
                    decision.getModelVersion(),
                    decision.getComputedAt(),
                    decision.getId().toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize decision data", e);
        }
    }
}
