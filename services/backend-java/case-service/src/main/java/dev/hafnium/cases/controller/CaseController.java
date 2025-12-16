package dev.hafnium.cases.controller;

import dev.hafnium.cases.dto.CaseResponse;
import dev.hafnium.cases.dto.CreateCaseRequest;
import dev.hafnium.cases.dto.UpdateCaseRequest;
import dev.hafnium.cases.service.CaseService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for case management.
 */
@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    /**
     * Create a new investigation case.
     */
    @PostMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request) {
        CaseResponse response = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get case by ID.
     */
    @GetMapping("/{caseId}")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> getCase(@PathVariable UUID caseId) {
        CaseResponse response = caseService.getCase(caseId);
        return ResponseEntity.ok(response);
    }

    /**
     * List cases with pagination.
     */
    @GetMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<Page<CaseResponse>> listCases(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        Page<CaseResponse> response = caseService.listCases(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a case.
     */
    @PatchMapping("/{caseId}")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> updateCase(
            @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseRequest request) {
        CaseResponse response = caseService.updateCase(caseId, request);
        return ResponseEntity.ok(response);
    }
}
