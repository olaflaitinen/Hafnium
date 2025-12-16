package dev.hafnium.risk.controller;

import dev.hafnium.risk.dto.RiskScoreRequest;
import dev.hafnium.risk.dto.RiskScoreResponse;
import dev.hafnium.risk.service.RiskScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for risk scoring API.
 */
@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskScoringService scoringService;

    /**
     * Compute a risk score for an entity.
     */
    @PostMapping("/score")
    @PreAuthorize("hasRole('ANALYST') or hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<RiskScoreResponse> computeRiskScore(
            @Valid @RequestBody RiskScoreRequest request) {
        RiskScoreResponse response = scoringService.computeRiskScore(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve cached risk score for an entity.
     */
    @GetMapping("/score/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ANALYST') or hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<RiskScoreResponse> getRiskScore(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        RiskScoreResponse response = scoringService.getRiskScore(entityType, entityId);
        return ResponseEntity.ok(response);
    }
}
