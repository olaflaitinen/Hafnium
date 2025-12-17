package dev.hafnium.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that extracts tenant context from JWT claims and populates the
 * TenantContext.
 *
 * <p>
 * This filter runs after Spring Security authentication and extracts:
 *
 * <ul>
 * <li>tenant_id from JWT claims or X-Tenant-ID header (for service-to-service
 * calls)
 * <li>actor_id from JWT subject claim
 * <li>trace_id from X-Trace-ID header or generates a new one
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TenantContextFilter.class);

    public static final String HEADER_TENANT_ID = "X-Tenant-ID";
    public static final String HEADER_TRACE_ID = "X-Trace-ID";
    public static final String CLAIM_TENANT_ID = "tenant_id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            extractAndSetContext(request);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void extractAndSetContext(HttpServletRequest request) {
        // Extract trace ID from header or generate new one
        String traceIdHeader = request.getHeader(HEADER_TRACE_ID);
        UUID traceId = traceIdHeader != null ? parseUuid(traceIdHeader, "trace_id") : UUID.randomUUID();
        TenantContext.setTraceId(traceId);

        // Extract from JWT if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            // Extract actor ID from subject
            String subject = jwt.getSubject();
            if (subject != null) {
                TenantContext.setActorId(subject);
            }

            // Extract tenant ID from JWT claim
            String tenantIdClaim = jwt.getClaimAsString(CLAIM_TENANT_ID);
            if (tenantIdClaim != null) {
                UUID tenantId = parseUuid(tenantIdClaim, CLAIM_TENANT_ID);
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                    return;
                }
            }
        }

        // Fall back to header for service-to-service calls
        String tenantIdHeader = request.getHeader(HEADER_TENANT_ID);
        if (tenantIdHeader != null) {
            UUID tenantId = parseUuid(tenantIdHeader, HEADER_TENANT_ID);
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }
        }
    }

    private UUID parseUuid(String value, String source) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid UUID format for {}: {}", source, value);
            return null;
        }
    }
}
