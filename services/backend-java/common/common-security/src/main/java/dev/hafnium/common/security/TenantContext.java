package dev.hafnium.common.security;

import java.util.Optional;
import java.util.UUID;
import lombok.Value;

/**
 * Holds tenant and user context for the current request.
 *
 * <p>
 * This context is populated by the security filter chain and propagated
 * throughout the request
 * lifecycle.
 */
@Value
public class TenantContext {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    UUID tenantId;
    UUID userId;
    String username;

    /**
     * Sets the tenant context for the current thread.
     *
     * @param context the tenant context
     */
    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    /**
     * Gets the current tenant context.
     *
     * @return the tenant context, or empty if not set
     */
    public static Optional<TenantContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Gets the current tenant context, throwing if not set.
     *
     * @return the tenant context
     * @throws IllegalStateException if context is not set
     */
    public static TenantContext require() {
        return get()
                .orElseThrow(() -> new IllegalStateException("TenantContext not initialized for request"));
    }

    /**
     * Gets the current tenant ID.
     *
     * @return the tenant ID
     * @throws IllegalStateException if context is not set
     */
    public static UUID requireTenantId() {
        return require().getTenantId();
    }

    /**
     * Gets the current user ID.
     *
     * @return the user ID
     * @throws IllegalStateException if context is not set
     */
    public static UUID requireUserId() {
        return require().getUserId();
    }

    /** Clears the tenant context for the current thread. */
    public static void clear() {
        CONTEXT.remove();
    }
}
