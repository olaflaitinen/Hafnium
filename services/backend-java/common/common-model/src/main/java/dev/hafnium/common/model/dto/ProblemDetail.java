package dev.hafnium.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

/**
 * RFC 7807 Problem Details response for error handling.
 *
 * <p>
 * Provides a standardized error response format across all API endpoints,
 * conforming to the
 * Problem Details specification (RFC 7807).
 */
public record ProblemDetail(
        @JsonProperty("type") URI type,
        @JsonProperty("title") String title,
        @JsonProperty("status") int status,
        @JsonProperty("detail") String detail,
        @JsonProperty("instance") URI instance,
        @JsonProperty("trace_id") String traceId) {

    /** Base URI for problem type definitions. */
    public static final String PROBLEM_TYPE_BASE = "https://api.hafnium.dev/problems/";

    /**
     * Creates a validation error problem detail.
     *
     * @param detail   The detailed error message
     * @param instance The request URI that caused the error
     * @param traceId  The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail validationError(String detail, URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "validation-error"),
                "Validation Error",
                400,
                detail,
                instance,
                traceId);
    }

    /**
     * Creates a not found problem detail.
     *
     * @param resourceType The type of resource that was not found
     * @param resourceId   The ID of the resource
     * @param instance     The request URI
     * @param traceId      The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail notFound(
            String resourceType, String resourceId, URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "not-found"),
                "Resource Not Found",
                404,
                String.format("%s with id '%s' was not found", resourceType, resourceId),
                instance,
                traceId);
    }

    /**
     * Creates a conflict problem detail.
     *
     * @param detail   The detailed error message
     * @param instance The request URI
     * @param traceId  The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail conflict(String detail, URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "conflict"),
                "Resource Conflict",
                409,
                detail,
                instance,
                traceId);
    }

    /**
     * Creates a forbidden problem detail.
     *
     * @param detail   The detailed error message
     * @param instance The request URI
     * @param traceId  The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail forbidden(String detail, URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "forbidden"),
                "Access Denied",
                403,
                detail,
                instance,
                traceId);
    }

    /**
     * Creates an internal server error problem detail.
     *
     * @param instance The request URI
     * @param traceId  The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail internalError(URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "internal-error"),
                "Internal Server Error",
                500,
                "An unexpected error occurred. Please contact support with the trace ID.",
                instance,
                traceId);
    }

    /**
     * Creates an unauthorized problem detail.
     *
     * @param detail   The detailed error message
     * @param instance The request URI
     * @param traceId  The distributed trace ID
     * @return A new ProblemDetail
     */
    public static ProblemDetail unauthorized(String detail, URI instance, String traceId) {
        return new ProblemDetail(
                URI.create(PROBLEM_TYPE_BASE + "unauthorized"),
                "Authentication Required",
                401,
                detail,
                instance,
                traceId);
    }
}
