package dev.hafnium.identity.controller;

import dev.hafnium.identity.dto.CreateCustomerRequest;
import dev.hafnium.identity.dto.CustomerResponse;
import dev.hafnium.identity.dto.UpdateCustomerRequest;
import dev.hafnium.identity.service.CustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer operations.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create a new customer.
     */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer by ID.
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ANALYST') or hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID customerId) {
        CustomerResponse response = customerService.getCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * List customers with pagination.
     */
    @GetMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        Page<CustomerResponse> response = customerService.listCustomers(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a customer.
     */
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }
}
