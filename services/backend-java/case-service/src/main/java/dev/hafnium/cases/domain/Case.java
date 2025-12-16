package dev.hafnium.cases.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Investigation case entity.
 */
@Entity
@Table(name = "cases", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "case_number", nullable = false)
    private String caseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CaseStatus status = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "alert_ids", columnDefinition = "jsonb")
    private String alertIds;

    @Column(columnDefinition = "jsonb")
    private String tags;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public enum CaseType {
        SAR,
        AML_INVESTIGATION,
        FRAUD,
        SANCTIONS,
        KYC_REMEDIATION,
        COMPLIANCE_REVIEW
    }

    public enum CaseStatus {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        PENDING_REVIEW,
        ESCALATED,
        CLOSED_CONFIRMED,
        CLOSED_FALSE_POSITIVE,
        CLOSED_NO_ACTION
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public boolean isClosed() {
        return status.name().startsWith("CLOSED_");
    }
}
