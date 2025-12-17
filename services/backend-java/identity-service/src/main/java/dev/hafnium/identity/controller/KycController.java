package dev.hafnium.identity.controller;

import dev.hafnium.identity.dto.KycInitiateRequest;
import dev.hafnium.identity.dto.KycWorkflowResponse;
import dev.hafnium.identity.service.KycWorkflowService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for KYC workflow operations.
 *
 * <p>
 * Provides endpoints for KYC workflow management per the OpenAPI specification.
 */
@RestController
@RequestMapping("/api/v1")
public class KycController {

    private final KycWorkflowService kycWorkflowService;

    public KycController(KycWorkflowService kycWorkflowService) {
        this.kycWorkflowService = kycWorkflowService;
    }

    /**
     * Initiates KYC verification for a customer.
     *
     * @param customerId The customer identifier
     * @param request    The KYC initiation request
     * @return The created workflow
     */
    @PostMapping("/customers/{customerId}/kyc")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<KycWorkflowResponse> initiateKyc(
            @PathVariable UUID customerId, @Valid @RequestBody KycInitiateRequest request) {
        KycWorkflowResponse workflow = kycWorkflowService.initiateKyc(customerId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(workflow);
    }

    /**
     * Gets the current KYC status for a customer.
     *
     * @param customerId The customer identifier
     * @return The current KYC workflow status
     */
    @GetMapping("/customers/{customerId}/kyc")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<KycWorkflowResponse> getKycStatus(@PathVariable UUID customerId) {
        KycWorkflowResponse workflow = kycWorkflowService.getKycStatus(customerId);
        return ResponseEntity.ok(workflow);
    }

    /**
     * Gets a KYC workflow by ID.
     *
     * @param workflowId The workflow identifier
     * @return The workflow details
     */
    @GetMapping("/kyc/requests/{workflowId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<KycWorkflowResponse> getWorkflow(@PathVariable UUID workflowId) {
        KycWorkflowResponse workflow = kycWorkflowService.getWorkflow(workflowId);
        return ResponseEntity.ok(workflow);
    }

    /**
     * Completes a KYC workflow with approval or rejection.
     *
     * @param workflowId The workflow identifier
     * @param request    The completion request containing 'approved' boolean and
     *                   'result' details
     * @return The updated workflow
     */
    @PostMapping("/kyc/requests/{workflowId}/complete")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<KycWorkflowResponse> completeWorkflow(
            @PathVariable UUID workflowId, @RequestBody Map<String, Object> request) {
        boolean approved = Boolean.TRUE.equals(request.get("approved"));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) request.getOrDefault("result", Map.of());

        KycWorkflowResponse workflow = kycWorkflowService.completeWorkflow(workflowId, approved, result);
        return ResponseEntity.ok(workflow);
    }
}
