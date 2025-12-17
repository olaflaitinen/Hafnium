package dev.hafnium.common.web;

import dev.hafnium.common.model.dto.ProblemDetail;
import dev.hafnium.common.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 *
 * <p>
 * Converts exceptions to RFC 7807 Problem Details responses with appropriate
 * HTTP status codes
 * and correlation IDs for debugging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex      The validation exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        LOG.warn("Validation error: {}", details);

        ProblemDetail problem = ProblemDetail.validationError(
                details, URI.create(request.getRequestURI()), getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles constraint violations from @Validated.
     *
     * @param ex      The constraint violation exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        LOG.warn("Constraint violation: {}", details);

        ProblemDetail problem = ProblemDetail.validationError(
                details, URI.create(request.getRequestURI()), getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles resource not found exceptions.
     *
     * @param ex      The not found exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        LOG.debug("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.notFound(
                ex.getResourceType(),
                ex.getResourceId(),
                URI.create(request.getRequestURI()),
                getTraceId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles resource conflict exceptions.
     *
     * @param ex      The conflict exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
            ResourceConflictException ex, HttpServletRequest request) {

        LOG.warn("Resource conflict: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.conflict(
                ex.getMessage(), URI.create(request.getRequestURI()), getTraceId());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles authentication exceptions.
     *
     * @param ex      The authentication exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        LOG.warn("Authentication failed: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.unauthorized(
                "Authentication required", URI.create(request.getRequestURI()), getTraceId());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    /**
     * Handles access denied exceptions.
     *
     * @param ex      The access denied exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        LOG.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forbidden(
                "You do not have permission to access this resource",
                URI.create(request.getRequestURI()),
                getTraceId());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex      The exception
     * @param request The HTTP request
     * @return Problem detail response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {

        LOG.error("Unexpected error processing request", ex);

        ProblemDetail problem = ProblemDetail.internalError(URI.create(request.getRequestURI()), getTraceId());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String getTraceId() {
        var traceId = TenantContext.getTraceId();
        return traceId != null ? traceId.toString() : "unknown";
    }
}
