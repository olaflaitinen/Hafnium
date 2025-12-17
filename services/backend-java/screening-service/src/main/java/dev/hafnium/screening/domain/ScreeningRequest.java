package dev.hafnium.screening.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Screening request domain entity.
 *
 * <p>
 * Represents a screening request against sanctions and PEP lists.
 */
@Entity
@Table(name = "screening_requests")
public class ScreeningRequest {

    @Id
    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> queryData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScreeningStatus status = ScreeningStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    private Map<String, Object> result;

    @Column(name = "match_count")
    private Integer matchCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public ScreeningRequest() {
        this.requestId = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public ScreeningRequest(UUID tenantId, EntityType entityType, UUID entityId, Map<String, Object> queryData) {
        this();
        this.tenantId = tenantId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.queryData = queryData;
    }

    // Getters and setters

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public Map<String, Object> getQueryData() {
        return queryData;
    }

    public void setQueryData(Map<String, Object> queryData) {
        this.queryData = queryData;
    }

    public ScreeningStatus getStatus() {
        return status;
    }

    public void setStatus(ScreeningStatus status) {
        this.status = status;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Integer getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(Integer matchCount) {
        this.matchCount = matchCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    /** Entity types that can be screened. */
    public enum EntityType {
        CUSTOMER,
        COUNTERPARTY,
        TRANSACTION
    }

    /** Screening request statuses. */
    public enum ScreeningStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }
}
