package dev.hafnium.riskengine.service;

import dev.hafnium.riskengine.dto.RiskScoreRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Client for ML inference service.
 *
 * <p>
 * Provides integration with the Python ai-inference service for ML-based risk
 * predictions.
 */
@Component
public class MlInferenceClient {

    private static final Logger LOG = LoggerFactory.getLogger(MlInferenceClient.class);

    @Value("${ml.inference.url:http://localhost:8085}")
    private String mlInferenceUrl;

    @Value("${ml.inference.enabled:false}")
    private boolean mlEnabled;

    @Value("${ml.model.id:risk-model-v1}")
    private String currentModelId;

    @Value("${ml.model.version:1.0.0}")
    private String currentModelVersion;

    /**
     * Gets ML prediction for risk scoring.
     *
     * @param request The risk score request
     * @return ML prediction result
     */
    @CircuitBreaker(name = "ml-inference", fallbackMethod = "predictFallback")
    public MlPrediction predict(RiskScoreRequest request) {
        if (!mlEnabled) {
            LOG.debug("ML inference disabled");
            return null;
        }

        // TODO: Implement actual ML inference call
        // This would call the ai-inference service
        LOG.info("ML prediction requested for entity {}", request.entityId());

        return null;
    }

    /**
     * Fallback when ML service is unavailable.
     *
     * @param request The request
     * @param ex      The exception
     * @return null to trigger rules-only scoring
     */
    public MlPrediction predictFallback(RiskScoreRequest request, Exception ex) {
        LOG.warn("ML inference fallback triggered: {}", ex.getMessage());
        return null;
    }

    /**
     * ML prediction result.
     *
     * @param score        The predicted risk score
     * @param modelId      The model identifier
     * @param modelVersion The model version
     */
    public record MlPrediction(BigDecimal score, String modelId, String modelVersion) {
    }
}
