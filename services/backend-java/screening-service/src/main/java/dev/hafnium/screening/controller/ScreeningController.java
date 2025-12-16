package dev.hafnium.screening.controller;

import dev.hafnium.screening.dto.ScreeningRequest;
import dev.hafnium.screening.dto.ScreeningResponse;
import dev.hafnium.screening.service.ScreeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for screening operations.
 */
@RestController
@RequestMapping("/api/v1/screening")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    /**
     * Screen a name against sanctions and PEP lists.
     */
    @PostMapping("/match")
    @PreAuthorize("hasRole('ANALYST') or hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ScreeningResponse> screenName(@Valid @RequestBody ScreeningRequest request) {
        ScreeningResponse response = screeningService.screenName(request);
        return ResponseEntity.ok(response);
    }
}
