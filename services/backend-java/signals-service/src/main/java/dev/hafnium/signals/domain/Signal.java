package dev.hafnium.signals.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Security signal entity.
 */
@Entity
@Table(name = "signals", schema = "signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private SignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String source;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "jsonb")
    private String indicators;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SignalStatus status = SignalStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    public enum SignalType {
        IP_THREAT,
        DEVICE_ANOMALY,
        BEHAVIOR_ANOMALY,
        CREDENTIAL_ABUSE,
        FRAUD_INDICATOR,
        BOT_ACTIVITY,
        GEO_ANOMALY,
        LINK_ANALYSIS
    }

    public enum Severity {
        INFO,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum SignalStatus {
        ACTIVE,
        ACKNOWLEDGED,
        RESOLVED,
        FALSE_POSITIVE
    }
}
