package dev.hafnium.common.model.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable audit event for compliance logging.
 *
 * <p>
 * Audit events capture all security-relevant actions in the system for
 * compliance and forensic
 * purposes. Each event includes a cryptographic hash for integrity verification
 * and optional
 * chaining to the previous event for tamper detection.
 *
 * <p>
 * This record is designed to be append-only; no update or delete operations are
 * permitted on
 * audit log entries in the database.
 */
public record AuditEvent(
        @JsonProperty("log_id") @NotNull UUID logId,
        @JsonProperty("tenant_id") @NotNull UUID tenantId,
        @JsonProperty("actor_id") @NotBlank String actorId,
        @JsonProperty("actor_type") @NotBlank String actorType,
        @JsonProperty("action") @NotBlank String action,
        @JsonProperty("resource_type") @NotBlank String resourceType,
        @JsonProperty("resource_id") String resourceId,
        @JsonProperty("details") Map<String, Object> details,
        @JsonProperty("previous_hash") String previousHash,
        @JsonProperty("entry_hash") @NotBlank String entryHash,
        @JsonProperty("ip_address") String ipAddress,
        @JsonProperty("user_agent") String userAgent,
        @JsonProperty("created_at") @NotNull Instant createdAt) {

    /** Actor type for human users. */
    public static final String ACTOR_TYPE_USER = "user";

    /** Actor type for service accounts. */
    public static final String ACTOR_TYPE_SERVICE = "service";

    /** Actor type for system-initiated actions. */
    public static final String ACTOR_TYPE_SYSTEM = "system";

    /**
     * Creates a new audit event with computed hash.
     *
     * @param tenantId     The tenant identifier
     * @param actorId      The identity performing the action
     * @param actorType    The type of actor (user, service, system)
     * @param action       The action being performed
     * @param resourceType The type of resource being acted upon
     * @param resourceId   The identifier of the specific resource
     * @param details      Additional context about the action (will be redacted of
     *                     PII)
     * @param previousHash The hash of the previous audit entry for chaining
     * @param ipAddress    The IP address of the request origin
     * @param userAgent    The user agent string
     * @return A new immutable AuditEvent
     */
    public static AuditEvent create(
            UUID tenantId,
            String actorId,
            String actorType,
            String action,
            String resourceType,
            String resourceId,
            Map<String, Object> details,
            String previousHash,
            String ipAddress,
            String userAgent) {

        UUID logId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        String entryHash = computeHash(logId, tenantId, actorId, action, resourceType, resourceId, createdAt);

        return new AuditEvent(
                logId,
                tenantId,
                actorId,
                actorType,
                action,
                resourceType,
                resourceId,
                details,
                previousHash,
                entryHash,
                ipAddress,
                userAgent,
                createdAt);
    }

    /**
     * Computes SHA-256 hash of the audit entry for integrity verification.
     *
     * @param logId        The log entry ID
     * @param tenantId     The tenant ID
     * @param actorId      The actor ID
     * @param action       The action
     * @param resourceType The resource type
     * @param resourceId   The resource ID
     * @param createdAt    The creation timestamp
     * @return Hex-encoded SHA-256 hash
     */
    private static String computeHash(
            UUID logId,
            UUID tenantId,
            String actorId,
            String action,
            String resourceType,
            String resourceId,
            Instant createdAt) {

        String data = String.join(
                "|",
                logId.toString(),
                tenantId.toString(),
                actorId,
                action,
                resourceType,
                resourceId != null ? resourceId : "",
                createdAt.toString());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies the integrity of this audit event by recomputing the hash.
     *
     * @return true if the entry hash matches the computed hash
     */
    public boolean verifyIntegrity() {
        String computed = computeHash(logId, tenantId, actorId, action, resourceType, resourceId, createdAt);
        return computed.equals(entryHash);
    }
}
