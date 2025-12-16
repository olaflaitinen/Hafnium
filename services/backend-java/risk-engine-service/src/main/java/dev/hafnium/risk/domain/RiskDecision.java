package dev.hafnium.risk.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * Risk decision entity representing a computed risk score with audit trail.
 */
@Entity
@Table(name = "risk_decisions", schema = "risk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "feature_version")
    private String featureVersion;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String reasons;

    @Column(name = "policy_actions", columnDefinition = "jsonb", nullable = false)
    private String policyActions;

    @Column(columnDefinition = "jsonb")
    private String context;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Column(name = "decision_metadata", columnDefinition = "jsonb")
    private String decisionMetadata;

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
