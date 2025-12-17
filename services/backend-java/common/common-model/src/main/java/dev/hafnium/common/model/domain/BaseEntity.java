package dev.hafnium.common.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base entity providing common fields for all domain entities.
 *
 * <p>
 * All domain entities in Hafnium extend this base to ensure consistent tenant
 * isolation, audit
 * timestamps, and identifier patterns.
 */
public abstract class BaseEntity {

    @JsonProperty("id")
    @NotNull
    private UUID id;

    @JsonProperty("tenant_id")
    @NotNull
    private UUID tenantId;

    @JsonProperty("created_at")
    @NotNull
    private Instant createdAt;

    @JsonProperty("updated_at")
    @NotNull
    private Instant updatedAt;

    @JsonProperty("version")
    private Long version;

    protected BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    protected BaseEntity(UUID tenantId) {
        this();
        this.tenantId = tenantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Updates the updatedAt timestamp to now. Should be called before any update
     * operation.
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }
}
