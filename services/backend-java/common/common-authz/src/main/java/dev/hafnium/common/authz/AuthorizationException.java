package dev.hafnium.common.authz;

/**
 * Exception thrown when an authorization check fails.
 *
 * <p>
 * This exception indicates a failure in the authorization process itself (e.g.,
 * OPA
 * unavailable), not a denial of access. For access denials, use Spring
 * Security's
 * AccessDeniedException.
 */
public class AuthorizationException extends RuntimeException {

    /**
     * Creates a new AuthorizationException.
     *
     * @param message The error message
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Creates a new AuthorizationException with a cause.
     *
     * @param message The error message
     * @param cause   The underlying cause
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
