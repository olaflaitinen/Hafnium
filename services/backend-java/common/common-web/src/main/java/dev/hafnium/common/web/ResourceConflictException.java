package dev.hafnium.common.web;

/**
 * Exception thrown when a resource conflict occurs.
 *
 * <p>
 * This exception is handled by {@link GlobalExceptionHandler} to return a 409
 * Conflict response
 * with RFC 7807 Problem Details.
 */
public class ResourceConflictException extends RuntimeException {

    /**
     * Creates a new ResourceConflictException.
     *
     * @param message The conflict description
     */
    public ResourceConflictException(String message) {
        super(message);
    }

    /**
     * Creates a conflict exception for duplicate resources.
     *
     * @param resourceType The type of resource
     * @param field        The field that has a duplicate value
     * @param value        The duplicate value
     * @return A new ResourceConflictException
     */
    public static ResourceConflictException duplicate(
            String resourceType, String field, String value) {
        return new ResourceConflictException(
                String.format("%s with %s '%s' already exists", resourceType, field, value));
    }
}
