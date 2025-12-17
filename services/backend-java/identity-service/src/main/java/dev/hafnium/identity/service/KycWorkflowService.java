package dev.hafnium.identity.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceConflictException;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.identity.domain.Customer;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.KycWorkflow;
import dev.hafnium.identity.domain.KycWorkflow.WorkflowStatus;
import dev.hafnium.identity.domain.KycWorkflow.WorkflowType;
import dev.hafnium.identity.dto.KycInitiateRequest;
import dev.hafnium.identity.dto.KycWorkflowResponse;
import dev.hafnium.identity.repository.CustomerRepository;
import dev.hafnium.identity.repository.KycWorkflowRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for KYC workflow orchestration.
 *
 * <p>
 * Manages KYC workflow lifecycle, step progression, and integration with AI
 * verification
 * services.
 */
@Service
@Transactional
public class KycWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(KycWorkflowService.class);

    private final KycWorkflowRepository workflowRepository;
    private final CustomerRepository customerRepository;
    private final KafkaEventPublisher eventPublisher;

    public KycWorkflowService(
            KycWorkflowRepository workflowRepository,
            CustomerRepository customerRepository,
            KafkaEventPublisher eventPublisher) {
        this.workflowRepository = workflowRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Initiates a new KYC verification workflow for a customer.
     *
     * @param customerId The customer identifier
     * @param request    The KYC initiation request
     * @return The created workflow response
     * @throws ResourceNotFoundException if the customer is not found
     * @throws ResourceConflictException if an active workflow already exists
     */
    public KycWorkflowResponse initiateKyc(UUID customerId, KycInitiateRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        // Verify customer exists
        Customer customer = customerRepository
                .findByTenantIdAndCustomerId(tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));

        // Check for existing active workflow
        if (workflowRepository.existsActiveWorkflow(tenantId, customerId)) {
            throw new ResourceConflictException("An active KYC workflow already exists for this customer");
        }

        // Create workflow
        WorkflowType workflowType = request.workflowType() != null ? request.workflowType() : WorkflowType.STANDARD;
        KycWorkflow workflow = new KycWorkflow(tenantId, customerId, workflowType);

        workflow = workflowRepository.save(workflow);

        // Update customer status
        customer.setStatus(CustomerStatus.DOCUMENTS_REQUIRED);
        customer.touch();
        customerRepository.save(customer);

        LOG.info(
                "Initiated KYC workflow {} for customer {} in tenant {}",
                workflow.getWorkflowId(),
                customerId,
                tenantId);

        // Emit event
        eventPublisher.publish(
                EventType.KYC_REQUESTED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "workflow_id", workflow.getWorkflowId(),
                        "customer_id", customerId,
                        "workflow_type", workflowType.name()));

        return toResponse(workflow);
    }

    /**
     * Gets the current KYC status for a customer.
     *
     * @param customerId The customer identifier
     * @return The latest workflow response
     * @throws ResourceNotFoundException if no workflow is found
     */
    @Transactional(readOnly = true)
    public KycWorkflowResponse getKycStatus(UUID customerId) {
        UUID tenantId = TenantContext.requireTenantId();

        KycWorkflow workflow = workflowRepository
                .findLatestByTenantIdAndCustomerId(tenantId, customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("KycWorkflow", "customer:" + customerId));

        return toResponse(workflow);
    }

    /**
     * Gets a workflow by ID.
     *
     * @param workflowId The workflow identifier
     * @return The workflow response
     * @throws ResourceNotFoundException if the workflow is not found
     */
    @Transactional(readOnly = true)
    public KycWorkflowResponse getWorkflow(UUID workflowId) {
        UUID tenantId = TenantContext.requireTenantId();

        KycWorkflow workflow = workflowRepository
                .findByTenantIdAndWorkflowId(tenantId, workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("KycWorkflow", workflowId.toString()));

        return toResponse(workflow);
    }

    /**
     * Completes a KYC workflow with a final decision.
     *
     * @param workflowId The workflow identifier
     * @param approved   Whether the KYC was approved
     * @param result     The verification result details
     * @return The updated workflow response
     */
    public KycWorkflowResponse completeWorkflow(
            UUID workflowId, boolean approved, Map<String, Object> result) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        KycWorkflow workflow = workflowRepository
                .findByTenantIdAndWorkflowId(tenantId, workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("KycWorkflow", workflowId.toString()));

        // Update workflow
        workflow.setStatus(approved ? WorkflowStatus.APPROVED : WorkflowStatus.REJECTED);
        workflow.setResult(result);
        workflow.setCompletedAt(Instant.now());
        workflow.setUpdatedAt(Instant.now());

        workflow = workflowRepository.save(workflow);

        // Update customer
        Customer customer = customerRepository
                .findByTenantIdAndCustomerId(tenantId, workflow.getCustomerId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Customer", workflow.getCustomerId().toString()));

        customer.setStatus(approved ? CustomerStatus.VERIFIED : CustomerStatus.REJECTED);
        customer.touch();
        customerRepository.save(customer);

        LOG.info(
                "Completed KYC workflow {} with status {} for customer {}",
                workflowId,
                workflow.getStatus(),
                workflow.getCustomerId());

        // Emit event
        eventPublisher.publish(
                EventType.KYC_COMPLETED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "workflow_id", workflow.getWorkflowId(),
                        "customer_id", workflow.getCustomerId(),
                        "status", workflow.getStatus().name(),
                        "approved", approved));

        // Emit customer verified event
        eventPublisher.publish(
                EventType.CUSTOMER_VERIFIED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "customer_id", workflow.getCustomerId(),
                        "workflow_id", workflow.getWorkflowId(),
                        "status", approved ? "approved" : "rejected"));

        return toResponse(workflow);
    }

    private KycWorkflowResponse toResponse(KycWorkflow workflow) {
        return new KycWorkflowResponse(
                workflow.getWorkflowId(),
                workflow.getCustomerId(),
                workflow.getWorkflowType().name().toLowerCase(),
                workflow.getStatus().name().toLowerCase(),
                workflow.getCurrentStep(),
                workflow.getSteps(),
                workflow.getCreatedAt(),
                workflow.getCompletedAt());
    }
}
