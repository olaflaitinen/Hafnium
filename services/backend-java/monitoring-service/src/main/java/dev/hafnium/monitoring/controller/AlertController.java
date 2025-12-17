package dev.hafnium.monitoring.controller;

import dev.hafnium.common.model.dto.PagedResponse;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.monitoring.domain.Alert;
import dev.hafnium.monitoring.domain.Alert.AlertStatus;
import dev.hafnium.monitoring.domain.Alert.Severity;
import dev.hafnium.monitoring.dto.AlertResponse;
import dev.hafnium.monitoring.repository.AlertRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for alert operations.
 */
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Lists alerts with optional filtering.
     *
     * @param status   Optional status filter
     * @param severity Optional severity filter
     * @param cursor   Optional pagination cursor
     * @param limit    Maximum results (default 50, max 100)
     * @return Paginated list of alerts
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<PagedResponse<List<AlertResponse>>> listAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int limit) {

        UUID tenantId = TenantContext.requireTenantId();
        limit = Math.min(limit, 100);

        int offset = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                offset = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                // Invalid cursor, start from beginning
            }
        }

        Page<Alert> page = alertRepository.findByTenantIdWithFilters(
                tenantId, status, severity, PageRequest.of(offset / limit, limit));

        List<AlertResponse> responses = page.getContent().stream().map(this::toResponse).toList();

        String nextCursor = page.hasNext() ? String.valueOf(offset + limit) : null;

        return ResponseEntity.ok(PagedResponse.of(responses, nextCursor, page.hasNext()));
    }

    /**
     * Gets an alert by ID.
     *
     * @param alertId The alert identifier
     * @return The alert details
     */
    @GetMapping("/{alertId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable UUID alertId) {
        UUID tenantId = TenantContext.requireTenantId();

        Alert alert = alertRepository
                .findByTenantIdAndAlertId(tenantId, alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId.toString()));

        return ResponseEntity.ok(toResponse(alert));
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getAlertId(),
                alert.getRuleId(),
                alert.getRuleName(),
                alert.getSeverity().name().toLowerCase(),
                alert.getScore(),
                alert.getTxnId(),
                alert.getCustomerId(),
                alert.getStatus().name().toLowerCase(),
                alert.getExplanation(),
                alert.getTriggeredConditions(),
                alert.getCaseId(),
                alert.getCreatedAt());
    }
}
