package dev.hafnium.signals.controller;

import dev.hafnium.signals.dto.SessionSignalRequest;
import dev.hafnium.signals.dto.SignalResponse;
import dev.hafnium.signals.service.SignalEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for signal evaluation.
 */
@RestController
@RequestMapping("/api/v1/signals")
public class SignalController {

    private final SignalEvaluationService signalService;

    public SignalController(SignalEvaluationService signalService) {
        this.signalService = signalService;
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
    public ResponseEntity<SignalResponse> evaluateSession(
            @Valid @RequestBody SessionSignalRequest request) {
        SignalResponse response = signalService.evaluateSession(request);
        return ResponseEntity.ok(response);
    }
}
