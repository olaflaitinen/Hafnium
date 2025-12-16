package dev.hafnium.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * KYC Workflow entity representing a verification workflow for a customer.
 */
@Entity
@Table(name = "kyc_workflows", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false)
    @Builder.Default
    private WorkflowType workflowType = WorkflowType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.INITIATED;

    @Column(name = "current_step")
    private String currentStep;

    @Column(columnDefinition = "jsonb")
    private String steps;

    @Column(columnDefinition = "jsonb")
    private String result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public void complete(WorkflowStatus finalStatus) {
        this.status = finalStatus;
        this.completedAt = Instant.now();
    }

    public enum WorkflowType {
        STANDARD,
        ENHANCED,
        SIMPLIFIED
    }

    public enum WorkflowStatus {
        INITIATED,
        DOCUMENTS_PENDING,
        DOCUMENTS_SUBMITTED,
        IN_REVIEW,
        APPROVED,
        REJECTED
    }
}
