package dev.hafnium.vault.controller;

import dev.hafnium.vault.dto.TokenizeRequest;
import dev.hafnium.vault.dto.TokenizeResponse;
import dev.hafnium.vault.service.TokenizationService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for vault operations.
 */
@RestController
@RequestMapping("/api/v1/vault")
public class VaultController {

    private final TokenizationService tokenizationService;

    public VaultController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    @PostMapping("/tokenize")
    @PreAuthorize("hasAnyRole('SERVICE', 'ADMIN')")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest request) {
        TokenizeResponse response = tokenizationService.tokenize(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detokenize")
    @PreAuthorize("hasAnyRole('SERVICE', 'ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, String>> detokenize(@RequestBody Map<String, String> tokens) {
        Map<String, String> values = tokenizationService.detokenizeAll(tokens);
        return ResponseEntity.ok(values);
    }
}
