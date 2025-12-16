package dev.hafnium.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * RFC 7807 Problem Details for HTTP APIs.
 *
 * <p>
 * All error responses must use this format for consistency.
 */
@Value
@Builder
@Jacksonized
public class ProblemDetail {

    @NotBlank
    String type;

    @NotBlank
    String title;

    @NotNull
    @Min(400)
    @Max(599)
    Integer status;

    String detail;

    String instance;

    @JsonProperty("trace_id")
    String traceId;

    List<FieldError> errors;

    @Value
    @Builder
    @Jacksonized
    public static class FieldError {
        String field;
        String code;
        String message;
    }

    public static ProblemDetail notFound(String resource, String id, String traceId) {
        return ProblemDetail.builder()
                .type("https://hafnium.dev/errors/not-found")
                .title("Resource Not Found")
                .status(404)
                .detail(String.format("%s with ID '%s' was not found", resource, id))
                .traceId(traceId)
                .build();
    }

    public static ProblemDetail validationFailed(List<FieldError> errors, String traceId) {
        return ProblemDetail.builder()
                .type("https://hafnium.dev/errors/validation-failed")
                .title("Validation Failed")
                .status(400)
                .detail("The request body contains invalid fields")
                .errors(errors)
                .traceId(traceId)
                .build();
    }

    public static ProblemDetail unauthorized(String detail, String traceId) {
        return ProblemDetail.builder()
                .type("https://hafnium.dev/errors/unauthorized")
                .title("Unauthorized")
                .status(401)
                .detail(detail)
                .traceId(traceId)
                .build();
    }

    public static ProblemDetail forbidden(String detail, String traceId) {
        return ProblemDetail.builder()
                .type("https://hafnium.dev/errors/forbidden")
                .title("Forbidden")
                .status(403)
                .detail(detail)
                .traceId(traceId)
                .build();
    }

    public static ProblemDetail internalError(String traceId) {
        return ProblemDetail.builder()
                .type("https://hafnium.dev/errors/internal-error")
                .title("Internal Server Error")
                .status(500)
                .detail("An unexpected error occurred. Please contact support with trace ID.")
                .traceId(traceId)
                .build();
    }
}
