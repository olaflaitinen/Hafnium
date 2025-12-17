package dev.hafnium.cases.service;

import dev.hafnium.cases.domain.Case;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Client for AI summarization service.
 *
 * <p>
 * Integrates with the ai-inference service to generate case summaries. Includes
 * circuit breaker
 * protection and fallback behavior.
 */
@Component
public class AiSummarizationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AiSummarizationClient.class);

    @Value("${ai.inference.url:http://localhost:8085}")
    private String aiInferenceUrl;

    @Value("${ai.inference.enabled:false}")
    private boolean aiEnabled;

    /**
     * Generates an AI summary for a case.
     *
     * @param caseEntity The case to summarize
     * @return The generated summary
     */
    @CircuitBreaker(name = "ai-summarization", fallbackMethod = "summarizeFallback")
    public String summarizeCase(Case caseEntity) {
        if (!aiEnabled) {
            LOG.debug("AI summarization disabled, using fallback");
            return generateFallbackSummary(caseEntity);
        }

        // TODO: Implement actual AI inference call
        // This would call the ai-inference service with case context
        LOG.info("AI summarization requested for case {}", caseEntity.getCaseId());

        return generateFallbackSummary(caseEntity);
    }

    /**
     * Fallback method when AI service is unavailable.
     *
     * @param caseEntity The case
     * @param ex         The exception
     * @return A basic summary
     */
    public String summarizeFallback(Case caseEntity, Exception ex) {
        LOG.warn("AI summarization fallback triggered: {}", ex.getMessage());
        return generateFallbackSummary(caseEntity);
    }

    private String generateFallbackSummary(Case caseEntity) {
        StringBuilder summary = new StringBuilder();
        summary.append("Case type: ").append(caseEntity.getCaseType().name()).append(". ");
        summary.append("Priority: ").append(caseEntity.getPriority().name()).append(". ");
        summary.append("Status: ").append(caseEntity.getStatus().name()).append(". ");

        if (caseEntity.getDescription() != null && !caseEntity.getDescription().isEmpty()) {
            summary.append("Description: ").append(caseEntity.getDescription());
        }

        if (caseEntity.getAlertIds() != null && !caseEntity.getAlertIds().isEmpty()) {
            summary.append(" Related alerts: ").append(caseEntity.getAlertIds().size()).append(".");
        }

        return summary.toString();
    }
}
