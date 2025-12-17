package dev.hafnium.cases.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.cases.domain.Case;
import dev.hafnium.cases.domain.Case.CaseStatus;
import dev.hafnium.cases.dto.CaseResponse;
import dev.hafnium.cases.dto.CreateCaseRequest;
import dev.hafnium.cases.dto.UpdateCaseRequest;
import dev.hafnium.cases.repository.CaseRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for case management operations.
 */
@Service
@Transactional
public class CaseService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final AiSummarizationClient aiClient;
    private final KafkaEventPublisher eventPublisher;

    public CaseService(
            CaseRepository caseRepository,
            AiSummarizationClient aiClient,
            KafkaEventPublisher eventPublisher) {
        this.caseRepository = caseRepository;
        this.aiClient = aiClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new case.
     *
     * @param request The case creation request
     * @return The created case response
     */
    public CaseResponse createCase(CreateCaseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        Case caseEntity = new Case();
        caseEntity.setTenantId(tenantId);
        caseEntity.setTitle(request.title());
        caseEntity.setDescription(request.description());
        caseEntity.setCaseType(request.caseType());
        caseEntity.setPriority(request.priority());
        caseEntity.setAssignedTo(request.assignedTo());
        caseEntity.setCustomerId(request.customerId());
        caseEntity.setAlertIds(request.alertIds());
        caseEntity.setDueDate(request.dueDate());
        caseEntity.setMetadata(request.metadata());

        caseEntity = caseRepository.save(caseEntity);

        LOG.info("Created case {} for tenant {}", caseEntity.getCaseId(), tenantId);

        // Emit event
        eventPublisher.publish(
                EventType.CASE_CREATED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "case_id", caseEntity.getCaseId(),
                        "case_type", caseEntity.getCaseType().name(),
                        "priority", caseEntity.getPriority().name(),
                        "customer_id", caseEntity.getCustomerId() != null ? caseEntity.getCustomerId() : ""));

        return toResponse(caseEntity);
    }

    /**
     * Gets a case by ID.
     *
     * @param caseId The case identifier
     * @return The case response
     */
    @Transactional(readOnly = true)
    public CaseResponse getCase(UUID caseId) {
        UUID tenantId = TenantContext.requireTenantId();

        Case caseEntity = caseRepository
                .findByTenantIdAndCaseId(tenantId, caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId.toString()));

        return toResponse(caseEntity);
    }

    /**
     * Lists cases with optional filtering.
     *
     * @param status   Optional status filter
     * @param pageable Pagination parameters
     * @return Page of case responses
     */
    @Transactional(readOnly = true)
    public Page<CaseResponse> listCases(CaseStatus status, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();

        return caseRepository
                .findByTenantIdWithFilters(tenantId, status, pageable)
                .map(this::toResponse);
    }

    /**
     * Updates a case.
     *
     * @param caseId  The case identifier
     * @param request The update request
     * @return The updated case response
     */
    public CaseResponse updateCase(UUID caseId, UpdateCaseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        Case caseEntity = caseRepository
                .findByTenantIdAndCaseId(tenantId, caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId.toString()));

        if (request.status() != null) {
            caseEntity.setStatus(request.status());
            if (request.status().name().startsWith("CLOSED")) {
                caseEntity.setClosedAt(Instant.now());
            }
        }
        if (request.priority() != null) {
            caseEntity.setPriority(request.priority());
        }
        if (request.assignedTo() != null) {
            caseEntity.setAssignedTo(request.assignedTo());
        }
        if (request.resolution() != null) {
            caseEntity.setResolution(request.resolution());
        }

        caseEntity.touch();
        caseEntity = caseRepository.save(caseEntity);

        LOG.info("Updated case {} for tenant {}", caseId, tenantId);

        // Emit event
        eventPublisher.publish(
                EventType.CASE_UPDATED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "case_id", caseEntity.getCaseId(),
                        "status", caseEntity.getStatus().name(),
                        "priority", caseEntity.getPriority().name()));

        return toResponse(caseEntity);
    }

    /**
     * Generates an AI summary for a case.
     *
     * @param caseId The case identifier
     * @return The updated case with AI summary
     */
    public CaseResponse generateAiSummary(UUID caseId) {
        UUID tenantId = TenantContext.requireTenantId();

        Case caseEntity = caseRepository
                .findByTenantIdAndCaseId(tenantId, caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId.toString()));

        String summary = aiClient.summarizeCase(caseEntity);
        caseEntity.setAiSummary(summary);
        caseEntity.touch();

        caseEntity = caseRepository.save(caseEntity);

        LOG.info("Generated AI summary for case {}", caseId);

        return toResponse(caseEntity);
    }

    private CaseResponse toResponse(Case caseEntity) {
        return new CaseResponse(
                caseEntity.getCaseId(),
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getCaseType().name().toLowerCase(),
                caseEntity.getPriority().name().toLowerCase(),
                caseEntity.getStatus().name().toLowerCase(),
                caseEntity.getAssignedTo(),
                caseEntity.getCustomerId(),
                caseEntity.getAlertIds(),
                caseEntity.getAiSummary(),
                caseEntity.getResolution(),
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt(),
                caseEntity.getClosedAt(),
                caseEntity.getDueDate());
    }
}
