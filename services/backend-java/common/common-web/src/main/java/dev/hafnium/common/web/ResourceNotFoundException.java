package dev.hafnium.common.web;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>
 * This exception is handled by {@link GlobalExceptionHandler} to return a 404
 * Not Found
 * response with RFC 7807 Problem Details.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    /**
     * Creates a new ResourceNotFoundException.
     *
     * @param resourceType The type of resource (e.g., "Customer", "Case")
     * @param resourceId   The ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id '%s' was not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Gets the resource type.
     *
     * @return The resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Gets the resource ID.
     *
     * @return The resource ID
     */
    public String getResourceId() {
        return resourceId;
    }
}
