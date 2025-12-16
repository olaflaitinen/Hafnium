package dev.hafnium.monitoring.controller;

import dev.hafnium.monitoring.dto.TransactionRequest;
import dev.hafnium.monitoring.dto.TransactionResponse;
import dev.hafnium.monitoring.service.TransactionMonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transaction monitoring.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionMonitoringService monitoringService;

    /**
     * Ingest a transaction for monitoring.
     */
    @PostMapping("/transactions")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> ingestTransaction(
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = monitoringService.ingestTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
