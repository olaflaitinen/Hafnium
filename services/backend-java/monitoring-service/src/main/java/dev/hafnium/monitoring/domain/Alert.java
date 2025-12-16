package dev.hafnium.monitoring.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Alert entity for suspicious activity.
 */
@Entity
@Table(name = "alerts", schema = "monitoring")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "rule_id")
    private String ruleId;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String reasons;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "case_id")
    private UUID caseId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public enum AlertType {
        TRANSACTION_MONITORING,
        VELOCITY_ALERT,
        COUNTRY_RISK,
        SCREENING_MATCH,
        UNUSUAL_ACTIVITY,
        STRUCTURING
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        OPEN,
        ASSIGNED,
        IN_REVIEW,
        ESCALATED,
        CLOSED_TRUE_POSITIVE,
        CLOSED_FALSE_POSITIVE
    }
}
