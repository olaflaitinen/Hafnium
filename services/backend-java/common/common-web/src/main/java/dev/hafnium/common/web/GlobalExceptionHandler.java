package dev.hafnium.common.web;

import dev.hafnium.common.model.ProblemDetail;
import io.opentelemetry.api.trace.Span;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler producing RFC 7807 Problem Detail responses.
 *
 * <p>
 * All error responses include trace ID for debugging.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex) {
        String traceId = getTraceId();

        List<ProblemDetail.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(
                        fe -> ProblemDetail.FieldError.builder()
                                .field(fe.getField())
                                .code(fe.getCode())
                                .message(fe.getDefaultMessage())
                                .build())
                .toList();

        log.warn("Validation failed: {} errors, traceId={}", errors.size(), traceId);

        return ResponseEntity.badRequest().body(ProblemDetail.validationFailed(errors, traceId));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        String traceId = getTraceId();

        List<ProblemDetail.FieldError> errors = ex.getConstraintViolations().stream()
                .map(
                        cv -> ProblemDetail.FieldError.builder()
                                .field(cv.getPropertyPath().toString())
                                .message(cv.getMessage())
                                .build())
                .toList();

        log.warn("Constraint violation: {} errors, traceId={}", errors.size(), traceId);

        return ResponseEntity.badRequest().body(ProblemDetail.validationFailed(errors, traceId));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        String traceId = getTraceId();
        log.info("Resource not found: {}, traceId={}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.notFound(ex.getResource(), ex.getId(), traceId));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        String traceId = getTraceId();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ProblemDetail.builder()
                                .type("https://hafnium.dev/errors/not-found")
                                .title("Not Found")
                                .status(404)
                                .detail("The requested resource was not found")
                                .traceId(traceId)
                                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        String traceId = getTraceId();
        log.warn("Authentication failed: {}, traceId={}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail.unauthorized("Authentication required", traceId));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        String traceId = getTraceId();
        log.warn("Access denied: {}, traceId={}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ProblemDetail.forbidden("You do not have permission to access this resource", traceId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unexpected error, traceId={}", traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProblemDetail.internalError(traceId));
    }

    private String getTraceId() {
        try {
            Span currentSpan = Span.current();
            if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                return currentSpan.getSpanContext().getTraceId();
            }
        } catch (Exception ignored) {
            // OpenTelemetry may not be available
        }
        return null;
    }
}
