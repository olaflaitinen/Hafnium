package dev.hafnium.riskengine.controller;

import dev.hafnium.riskengine.dto.RiskScoreRequest;
import dev.hafnium.riskengine.dto.RiskScoreResponse;
import dev.hafnium.riskengine.service.RiskScoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for risk scoring operations.
 */
@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskScoringService riskScoringService;

    public RiskController(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    @PostMapping("/score")
    @PreAuthorize("hasAnyRole('SERVICE', 'ANALYST', 'ADMIN')")
    public ResponseEntity<RiskScoreResponse> calculateRiskScore(
            @Valid @RequestBody RiskScoreRequest request) {
        RiskScoreResponse response = riskScoringService.calculateRiskScore(request);
        return ResponseEntity.ok(response);
    }
}
