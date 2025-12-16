package dev.hafnium.cases.service;

import dev.hafnium.cases.domain.Case;
import dev.hafnium.cases.domain.CaseEvent;
import dev.hafnium.cases.dto.CreateCaseRequest;
import dev.hafnium.cases.dto.CaseResponse;
import dev.hafnium.cases.dto.UpdateCaseRequest;
import dev.hafnium.cases.repository.CaseEventRepository;
import dev.hafnium.cases.repository.CaseRepository;
import dev.hafnium.common.kafka.EventPublisher;
import dev.hafnium.common.kafka.Topics;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Case management service with state machine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseEventRepository eventRepository;
    private final EventPublisher eventPublisher;

    private final AtomicLong caseCounter = new AtomicLong(System.currentTimeMillis());

    @Value("${hafnium.case.sla-days.low:30}")
    private int slaLow;

    @Value("${hafnium.case.sla-days.medium:14}")
    private int slaMedium;

    @Value("${hafnium.case.sla-days.high:7}")
    private int slaHigh;

    @Value("${hafnium.case.sla-days.critical:3}")
    private int slaCritical;

    /**
     * Creates a new investigation case.
     */
    @Transactional
    public CaseResponse createCase(CreateCaseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.requireUserId();

        String caseNumber = generateCaseNumber();
        Case.Priority priority = Case.Priority.valueOf(request.priority().toUpperCase());

        Case investigationCase = Case.builder()
                .tenantId(tenantId)
                .caseNumber(caseNumber)
                .caseType(Case.CaseType.valueOf(request.caseType().toUpperCase()))
                .status(Case.CaseStatus.OPEN)
                .priority(priority)
                .subject(request.subject())
                .description(request.description())
                .customerId(request.customerId() != null ? UUID.fromString(request.customerId()) : null)
                .slaDueAt(calculateSlaDue(priority))
                .build();

        investigationCase = caseRepository.save(investigationCase);

        // Create initial event
        recordEvent(investigationCase, CaseEvent.EventType.CREATED, null, Case.CaseStatus.OPEN, userId, null);

        log.info("Case created: id={}, caseNumber={}", investigationCase.getId(), caseNumber);

        // Publish event
        eventPublisher.publish(
                Topics.CASE_CREATED,
                "case.created",
                "1.0.0",
                investigationCase.getId().toString(),
                investigationCase);

        return toResponse(investigationCase);
    }

    /**
     * Gets a case by ID.
     */
    @Transactional(readOnly = true)
    public CaseResponse getCase(UUID caseId) {
        UUID tenantId = TenantContext.requireTenantId();

        Case investigationCase = caseRepository.findByIdAndTenantId(caseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        return toResponse(investigationCase);
    }

    /**
     * Lists cases with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CaseResponse> listCases(String status, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();

        Page<Case> cases;
        if (status != null && !status.isBlank()) {
            Case.CaseStatus caseStatus = Case.CaseStatus.valueOf(status.toUpperCase());
            cases = caseRepository.findByTenantIdAndStatus(tenantId, caseStatus, pageable);
        } else {
            cases = caseRepository.findByTenantId(tenantId, pageable);
        }

        return cases.map(this::toResponse);
    }

    /**
     * Updates a case status.
     */
    @Transactional
    public CaseResponse updateCase(UUID caseId, UpdateCaseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.requireUserId();

        Case investigationCase = caseRepository.findByIdAndTenantId(caseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        Case.CaseStatus oldStatus = investigationCase.getStatus();

        if (request.status() != null) {
            Case.CaseStatus newStatus = Case.CaseStatus.valueOf(request.status().toUpperCase());
            validateTransition(oldStatus, newStatus);
            investigationCase.setStatus(newStatus);

            if (newStatus.name().startsWith("CLOSED_")) {
                investigationCase.setClosedAt(Instant.now());
            }

            recordEvent(investigationCase, CaseEvent.EventType.STATUS_CHANGED, oldStatus, newStatus, userId,
                    request.notes());
        }

        if (request.assignedTo() != null) {
            investigationCase.setAssignedTo(UUID.fromString(request.assignedTo()));
            if (investigationCase.getStatus() == Case.CaseStatus.OPEN) {
                investigationCase.setStatus(Case.CaseStatus.ASSIGNED);
            }
            recordEvent(investigationCase, CaseEvent.EventType.ASSIGNED, oldStatus, investigationCase.getStatus(),
                    userId, null);
        }

        if (request.priority() != null) {
            investigationCase.setPriority(Case.Priority.valueOf(request.priority().toUpperCase()));
            investigationCase.setSlaDueAt(calculateSlaDue(investigationCase.getPriority()));
            recordEvent(investigationCase, CaseEvent.EventType.PRIORITY_CHANGED, null, null, userId, null);
        }

        investigationCase = caseRepository.save(investigationCase);

        // Publish event
        eventPublisher.publish(
                Topics.CASE_UPDATED,
                "case.updated",
                "1.0.0",
                investigationCase.getId().toString(),
                investigationCase);

        return toResponse(investigationCase);
    }

    private String generateCaseNumber() {
        return String.format("CASE-%d", caseCounter.incrementAndGet());
    }

    private Instant calculateSlaDue(Case.Priority priority) {
        int days = switch (priority) {
            case LOW -> slaLow;
            case MEDIUM -> slaMedium;
            case HIGH -> slaHigh;
            case CRITICAL -> slaCritical;
        };
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    private void validateTransition(Case.CaseStatus from, Case.CaseStatus to) {
        // Valid transitions
        Map<Case.CaseStatus, java.util.List<Case.CaseStatus>> validTransitions = Map.of(
                Case.CaseStatus.OPEN,
                java.util.List.of(Case.CaseStatus.ASSIGNED, Case.CaseStatus.IN_PROGRESS,
                        Case.CaseStatus.CLOSED_NO_ACTION),
                Case.CaseStatus.ASSIGNED, java.util.List.of(Case.CaseStatus.IN_PROGRESS, Case.CaseStatus.OPEN),
                Case.CaseStatus.IN_PROGRESS,
                java.util.List.of(Case.CaseStatus.PENDING_REVIEW, Case.CaseStatus.ESCALATED,
                        Case.CaseStatus.CLOSED_CONFIRMED, Case.CaseStatus.CLOSED_FALSE_POSITIVE),
                Case.CaseStatus.PENDING_REVIEW,
                java.util.List.of(Case.CaseStatus.IN_PROGRESS, Case.CaseStatus.CLOSED_CONFIRMED,
                        Case.CaseStatus.CLOSED_FALSE_POSITIVE),
                Case.CaseStatus.ESCALATED,
                java.util.List.of(Case.CaseStatus.IN_PROGRESS, Case.CaseStatus.CLOSED_CONFIRMED));

        java.util.List<Case.CaseStatus> allowed = validTransitions.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", from, to));
        }
    }

    private void recordEvent(Case investigationCase, CaseEvent.EventType eventType,
            Case.CaseStatus from, Case.CaseStatus to, UUID actorId, String notes) {

        CaseEvent event = CaseEvent.builder()
                .tenantId(investigationCase.getTenantId())
                .investigationCase(investigationCase)
                .eventType(eventType)
                .fromState(from)
                .toState(to)
                .actorId(actorId)
                .notes(notes)
                .build();

        eventRepository.save(event);
    }

    private CaseResponse toResponse(Case c) {
        return new CaseResponse(
                c.getId(),
                c.getCaseNumber(),
                c.getCaseType().name(),
                c.getStatus().name(),
                c.getPriority().name(),
                c.getSubject(),
                c.getDescription(),
                c.getCustomerId(),
                c.getAssignedTo(),
                c.getSlaDueAt(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getClosedAt());
    }
}
