package dev.hafnium.common.authz;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * OPA authorization request.
 */
@Value
@Builder
@Jacksonized
public class AuthzRequest {

    Input input;

    @Value
    @Builder
    @Jacksonized
    public static class Input {
        @JsonProperty("tenant_id")
        String tenantId;

        @JsonProperty("user_id")
        String userId;

        Set<String> roles;

        String action;

        String resource;

        @JsonProperty("resource_id")
        String resourceId;

        Map<String, Object> context;
    }

    public static AuthzRequest of(
            String tenantId,
            String userId,
            Set<String> roles,
            String action,
            String resource,
            String resourceId) {
        return AuthzRequest.builder()
                .input(
                        Input.builder()
                                .tenantId(tenantId)
                                .userId(userId)
                                .roles(roles)
                                .action(action)
                                .resource(resource)
                                .resourceId(resourceId)
                                .build())
                .build();
    }

    public static AuthzRequest of(
            String tenantId,
            String userId,
            Set<String> roles,
            String action,
            String resource,
            String resourceId,
            Map<String, Object> context) {
        return AuthzRequest.builder()
                .input(
                        Input.builder()
                                .tenantId(tenantId)
                                .userId(userId)
                                .roles(roles)
                                .action(action)
                                .resource(resource)
                                .resourceId(resourceId)
                                .context(context)
                                .build())
                .build();
    }
}
