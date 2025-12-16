package dev.hafnium.common.web;

import lombok.Getter;

/**
 * Exception thrown when a requested resource is not found.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resource;
    private final String id;

    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s with ID '%s' was not found", resource, id));
        this.resource = resource;
        this.id = id;
    }

    public ResourceNotFoundException(String resource, Object id) {
        this(resource, String.valueOf(id));
    }
}
