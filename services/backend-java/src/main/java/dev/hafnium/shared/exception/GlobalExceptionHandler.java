package dev.hafnium.shared.exception;

import java.net.URI;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that converts exceptions to RFC 7807 Problem
 * Details.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String BASE_TYPE_URI = "https://hafnium.dev/errors/";

    /**
     * Handle resource not found exceptions.
     *
     * @param ex The exception
     * @return Problem detail response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage());

        problem.setType(URI.create(BASE_TYPE_URI + "resource-not-found"));
        problem.setTitle("Resource Not Found");
        problem.setProperty("resourceType", ex.getResourceType());
        problem.setProperty("resourceId", ex.getResourceId());
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    /**
     * Handle validation exceptions.
     *
     * @param ex The exception
     * @return Problem detail response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request body contains invalid fields");

        problem.setType(URI.create(BASE_TYPE_URI + "validation-failed"));
        problem.setTitle("Validation Failed");

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getCode(),
                        error.getDefaultMessage()))
                .toList();

        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    /**
     * Handle generic exceptions.
     *
     * @param ex The exception
     * @return Problem detail response
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");

        problem.setType(URI.create(BASE_TYPE_URI + "internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    /**
     * Validation error record.
     */
    public record ValidationError(String field, String code, String message) {
    }
}
