package dev.hafnium.monitoring.controller;

import dev.hafnium.monitoring.dto.TransactionRequest;
import dev.hafnium.monitoring.dto.TransactionResponse;
import dev.hafnium.monitoring.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Ingests a new transaction.
     *
     * @param request The transaction data
     * @return The ingested transaction with alert count
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'SERVICE', 'ADMIN')")
    public ResponseEntity<TransactionResponse> ingestTransaction(
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.ingestTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
