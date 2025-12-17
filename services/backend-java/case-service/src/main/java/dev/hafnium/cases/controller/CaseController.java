package dev.hafnium.cases.controller;

import dev.hafnium.common.model.dto.PagedResponse;
import dev.hafnium.cases.domain.Case.CaseStatus;
import dev.hafnium.cases.dto.CaseResponse;
import dev.hafnium.cases.dto.CreateCaseRequest;
import dev.hafnium.cases.dto.UpdateCaseRequest;
import dev.hafnium.cases.service.CaseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for case operations.
 */
@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request) {
        CaseResponse response = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<PagedResponse<List<CaseResponse>>> listCases(
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int limit) {

        limit = Math.min(limit, 100);
        int offset = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                offset = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                // Invalid cursor
            }
        }

        Page<CaseResponse> page = caseService.listCases(status, PageRequest.of(offset / limit, limit));
        String nextCursor = page.hasNext() ? String.valueOf(offset + limit) : null;

        return ResponseEntity.ok(PagedResponse.of(page.getContent(), nextCursor, page.hasNext()));
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<CaseResponse> getCase(@PathVariable UUID caseId) {
        CaseResponse response = caseService.getCase(caseId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{caseId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<CaseResponse> updateCase(
            @PathVariable UUID caseId, @Valid @RequestBody UpdateCaseRequest request) {
        CaseResponse response = caseService.updateCase(caseId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{caseId}/summarize")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<CaseResponse> generateSummary(@PathVariable UUID caseId) {
        CaseResponse response = caseService.generateAiSummary(caseId);
        return ResponseEntity.ok(response);
    }
}
