package dev.hafnium.vault.controller;

import dev.hafnium.vault.dto.*;
import dev.hafnium.vault.service.TokenizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final TokenizationService tokenizationService;

    @PostMapping("/tokenize")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest request) {
        return ResponseEntity.ok(tokenizationService.tokenize(request));
    }

    @PostMapping("/detokenize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DetokenizeResponse> detokenize(@Valid @RequestBody DetokenizeRequest request) {
        return ResponseEntity.ok(tokenizationService.detokenize(request));
    }
}
