package dev.hafnium.common.authz;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hafnium.common.security.TenantContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for Open Policy Agent (OPA) authorization decisions.
 *
 * <p>
 * This client sends authorization requests to OPA and interprets the policy
 * decisions. It
 * includes circuit breaker protection and fallback behavior for resilience.
 */
@Component
public class OpaAuthorizationClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpaAuthorizationClient.class);

    private static final String POLICY_PATH = "/v1/data/hafnium/authz/allow";
    private static final Duration TIMEOUT = Duration.ofMillis(500);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${opa.url:http://localhost:8181}")
    private String opaUrl;

    @Value("${opa.fail-open:false}")
    private boolean failOpen;

    public OpaAuthorizationClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    /**
     * Checks if the current request is authorized.
     *
     * @param method           The HTTP method
     * @param path             The request path
     * @param roles            The user's roles
     * @param resourceTenantId The tenant ID of the resource being accessed
     * @return true if authorized, false otherwise
     */
    @CircuitBreaker(name = "opa", fallbackMethod = "authorizeFallback")
    public boolean isAuthorized(
            String method, String path, List<String> roles, UUID resourceTenantId) {

        UUID userTenantId = TenantContext.getTenantId();
        String actorId = TenantContext.getActorId();

        OpaRequest request = new OpaRequest(
                new OpaInput(method, path, roles, userTenantId, resourceTenantId, actorId));

        try {
            OpaResponse response = webClient.post()
                    .uri(opaUrl + POLICY_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpaResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            boolean allowed = response != null && Boolean.TRUE.equals(response.result());

            LOG.debug("OPA authorization for {} {}: {} (tenant: {}, resource tenant: {})",
                    method, path, allowed, userTenantId, resourceTenantId);

            return allowed;

        } catch (Exception e) {
            LOG.error("OPA authorization check failed", e);
            throw new AuthorizationException("Authorization check failed", e);
        }
    }

    /**
     * Fallback method when OPA is unavailable.
     *
     * @param method           The HTTP method
     * @param path             The request path
     * @param roles            The user's roles
     * @param resourceTenantId The tenant ID of the resource
     * @param ex               The exception that triggered the fallback
     * @return Authorization decision based on fail-open configuration
     */
    public boolean authorizeFallback(
            String method, String path, List<String> roles, UUID resourceTenantId, Exception ex) {

        LOG.warn("OPA fallback triggered for {} {}: {}", method, path, ex.getMessage());

        if (failOpen) {
            LOG.warn("OPA fail-open enabled, allowing request");
            return true;
        }

        // In fail-closed mode, deny by default
        return false;
    }

    /**
     * Checks tenant access authorization.
     *
     * @param resourceTenantId The tenant ID of the resource being accessed
     * @return true if the current user can access resources in this tenant
     */
    public boolean canAccessTenant(UUID resourceTenantId) {
        UUID userTenantId = TenantContext.getTenantId();

        // Same tenant always allowed
        if (userTenantId != null && userTenantId.equals(resourceTenantId)) {
            return true;
        }

        // Cross-tenant access requires admin role
        return isAuthorized("GET", "/admin/tenants", List.of("admin"), resourceTenantId);
    }

    /** OPA request wrapper. */
    public record OpaRequest(@JsonProperty("input") OpaInput input) {
    }

    /** OPA input structure matching the Rego policy expectations. */
    public record OpaInput(
            @JsonProperty("method") String method,
            @JsonProperty("path") String path,
            @JsonProperty("roles") List<String> roles,
            @JsonProperty("tenant_id") UUID tenantId,
            @JsonProperty("resource_tenant") UUID resourceTenantId,
            @JsonProperty("actor_id") String actorId) {
    }

    /** OPA response structure. */
    public record OpaResponse(@JsonProperty("result") Boolean result) {
    }
}
