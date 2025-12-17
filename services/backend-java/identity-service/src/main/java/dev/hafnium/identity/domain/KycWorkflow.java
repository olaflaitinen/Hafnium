package dev.hafnium.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * KYC Workflow domain entity.
 *
 * <p>
 * Represents a KYC verification workflow for a customer. Tracks the workflow
 * state, steps, and
 * verification results.
 */
@Entity
@Table(name = "kyc_workflows")
public class KycWorkflow {

    @Id
    @Column(name = "workflow_id")
    private UUID workflowId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false)
    private WorkflowType workflowType = WorkflowType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStatus status = WorkflowStatus.INITIATED;

    @Column(name = "current_step")
    private String currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", columnDefinition = "jsonb")
    private List<WorkflowStep> steps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    private Map<String, Object> result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public KycWorkflow() {
        this.workflowId = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public KycWorkflow(UUID tenantId, UUID customerId, WorkflowType workflowType) {
        this();
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.workflowType = workflowType;
        initializeSteps();
    }

    private void initializeSteps() {
        this.steps = List.of(
                new WorkflowStep("document_upload", StepStatus.PENDING, null),
                new WorkflowStep("document_verification", StepStatus.PENDING, null),
                new WorkflowStep("face_match", StepStatus.PENDING, null),
                new WorkflowStep("liveness_check", StepStatus.PENDING, null),
                new WorkflowStep("screening", StepStatus.PENDING, null),
                new WorkflowStep("final_review", StepStatus.PENDING, null));
        this.currentStep = "document_upload";
    }

    // Getters and setters

    public UUID getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    /** Workflow types per OpenAPI specification. */
    public enum WorkflowType {
        STANDARD,
        ENHANCED,
        SIMPLIFIED
    }

    /** Workflow statuses per OpenAPI specification. */
    public enum WorkflowStatus {
        INITIATED,
        DOCUMENTS_PENDING,
        DOCUMENTS_SUBMITTED,
        IN_REVIEW,
        APPROVED,
        REJECTED
    }

    /** Step statuses. */
    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }

    /** Workflow step record. */
    public record WorkflowStep(
            String step,
            StepStatus status,
            Instant completedAt) {
    }
}
