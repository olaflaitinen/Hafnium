package dev.hafnium.common.authz;

import dev.hafnium.common.security.TenantContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for OPA policy decisions.
 *
 * <p>
 * Uses circuit breaker pattern for resilience.
 */
@Slf4j
public class OpaClient {

    private static final String CIRCUIT_BREAKER_NAME = "opa";
    private static final Duration TIMEOUT = Duration.ofMillis(500);

    private final WebClient webClient;
    private final String policyPath;

    public OpaClient(String opaUrl, String policyPath) {
        this.webClient = WebClient.builder().baseUrl(opaUrl).build();
        this.policyPath = policyPath;
    }

    /**
     * Checks if the current user is authorized to perform an action on a resource.
     *
     * @param action     the action (e.g., "read", "write", "delete")
     * @param resource   the resource type (e.g., "case", "customer")
     * @param resourceId the resource identifier
     * @return true if authorized
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallback")
    public boolean isAllowed(String action, String resource, String resourceId) {
        TenantContext ctx = TenantContext.require();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        AuthzRequest request = AuthzRequest.of(
                ctx.getTenantId().toString(),
                ctx.getUserId().toString(),
                roles,
                action,
                resource,
                resourceId);

        AuthzResponse response = webClient
                .post()
                .uri(policyPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthzResponse.class)
                .timeout(TIMEOUT)
                .block();

        if (response == null) {
            log.warn("OPA returned null response for action={}, resource={}", action, resource);
            return false;
        }

        if (!response.isAllowed()) {
            log.info(
                    "Authorization denied: action={}, resource={}, resourceId={}, reasons={}",
                    action,
                    resource,
                    resourceId,
                    response.getReasons());
        }

        return response.isAllowed();
    }

    /**
     * Fallback when OPA is unavailable.
     *
     * <p>
     * Default deny for safety.
     */
    @SuppressWarnings("unused")
    private boolean fallback(String action, String resource, String resourceId, Throwable t) {
        log.error("OPA circuit breaker open, denying access: action={}, resource={}", action, resource, t);
        return false;
    }

    /**
     * Checks authorization and throws AccessDeniedException if denied.
     *
     * @param action     the action
     * @param resource   the resource type
     * @param resourceId the resource identifier
     * @throws org.springframework.security.access.AccessDeniedException if not
     *                                                                   authorized
     */
    public void requireAuthorization(String action, String resource, String resourceId) {
        if (!isAllowed(action, resource, resourceId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    String.format("Access denied for %s on %s/%s", action, resource, resourceId));
        }
    }
}
