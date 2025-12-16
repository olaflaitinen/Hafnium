package dev.hafnium.risk.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for AI inference service.
 */
@Slf4j
@Component
public class AiInferenceClient {

    private final WebClient webClient;
    private final Duration timeout;

    public AiInferenceClient(
            @Value("${hafnium.ai-inference.base-url:http://localhost:8001}") String baseUrl,
            @Value("${hafnium.ai-inference.timeout-ms:500}") int timeoutMs) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    /**
     * Gets a risk score from the AI inference service.
     */
    @CircuitBreaker(name = "ai-inference", fallbackMethod = "getScoreFallback")
    public Double getScore(String entityType, String entityId, Map<String, Double> features) {
        log.debug("Calling AI inference for entity: {}/{}", entityType, entityId);

        InferenceRequest request = new InferenceRequest(entityType, entityId, features);

        InferenceResponse response = webClient.post()
                .uri("/api/v1/risk/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(InferenceResponse.class)
                .timeout(timeout)
                .block();

        if (response != null) {
            log.debug("AI inference returned score: {}", response.score());
            return response.score();
        }

        return null;
    }

    @SuppressWarnings("unused")
    private Double getScoreFallback(String entityType, String entityId, Map<String, Double> features, Throwable t) {
        log.warn("AI inference fallback triggered for {}/{}: {}", entityType, entityId, t.getMessage());
        return null;
    }

    private record InferenceRequest(String entityType, String entityId, Map<String, Double> features) {
    }

    private record InferenceResponse(Double score, String modelVersion) {
    }
}
