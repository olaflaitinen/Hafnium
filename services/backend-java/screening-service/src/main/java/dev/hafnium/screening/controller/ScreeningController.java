package dev.hafnium.screening.controller;

import dev.hafnium.screening.dto.ScreeningMatchRequest;
import dev.hafnium.screening.dto.ScreeningMatchResponse;
import dev.hafnium.screening.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for screening operations.
 *
 * <p>
 * Provides endpoints for sanctions and PEP screening.
 */
@RestController
@RequestMapping("/api/v1/screening")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    /**
     * Performs screening match against sanctions and PEP lists.
     *
     * @param request The screening request
     * @return The screening response with matches
     */
    @PostMapping("/match")
    @PreAuthorize("hasAnyRole('ANALYST', 'OPERATOR', 'SERVICE', 'ADMIN')")
    public ResponseEntity<ScreeningMatchResponse> performScreening(
            @Valid @RequestBody ScreeningMatchRequest request) {
        ScreeningMatchResponse response = screeningService.performScreening(request);
        return ResponseEntity.ok(response);
    }
}
