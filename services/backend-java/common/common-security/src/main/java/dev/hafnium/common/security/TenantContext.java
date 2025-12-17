package dev.hafnium.common.security;

import java.util.UUID;

/**
 * Thread-local holder for tenant context propagation.
 *
 * <p>
 * The TenantContext provides a mechanism for propagating tenant isolation
 * across all layers of
 * the application. It is populated by the {@link TenantContextFilter} from JWT
 * claims and request
 * headers, and should be cleared after each request.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * UUID tenantId = TenantContext.getTenantId();
 * if (tenantId != null) {
 *     // Use tenantId for data access filtering
 * }
 * }</pre>
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ACTOR_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> TRACE_ID = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    /**
     * Sets the current tenant ID.
     *
     * @param tenantId The tenant UUID
     */
    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * Gets the current tenant ID.
     *
     * @return The tenant UUID, or null if not set
     */
    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Gets the current tenant ID, throwing if not set.
     *
     * @return The tenant UUID
     * @throws IllegalStateException if tenant context is not initialized
     */
    public static UUID requireTenantId() {
        UUID tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not initialized");
        }
        return tenantId;
    }

    /**
     * Sets the current actor ID (user or service identity).
     *
     * @param actorId The actor identifier
     */
    public static void setActorId(String actorId) {
        ACTOR_ID.set(actorId);
    }

    /**
     * Gets the current actor ID.
     *
     * @return The actor identifier, or null if not set
     */
    public static String getActorId() {
        return ACTOR_ID.get();
    }

    /**
     * Gets the current actor ID, throwing if not set.
     *
     * @return The actor identifier
     * @throws IllegalStateException if actor context is not initialized
     */
    public static String requireActorId() {
        String actorId = ACTOR_ID.get();
        if (actorId == null) {
            throw new IllegalStateException("Actor context not initialized");
        }
        return actorId;
    }

    /**
     * Sets the current trace ID for distributed tracing.
     *
     * @param traceId The trace UUID
     */
    public static void setTraceId(UUID traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * Gets the current trace ID.
     *
     * @return The trace UUID, or null if not set
     */
    public static UUID getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * Gets the current trace ID, generating one if not set.
     *
     * @return The trace UUID
     */
    public static UUID getOrCreateTraceId() {
        UUID traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = UUID.randomUUID();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    /** Clears all context values. Should be called at the end of each request. */
    public static void clear() {
        TENANT_ID.remove();
        ACTOR_ID.remove();
        TRACE_ID.remove();
    }
}
