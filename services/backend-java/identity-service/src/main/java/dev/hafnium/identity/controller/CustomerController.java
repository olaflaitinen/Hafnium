package dev.hafnium.identity.controller;

import dev.hafnium.common.model.dto.PagedResponse;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.Customer.RiskTier;
import dev.hafnium.identity.dto.CreateCustomerRequest;
import dev.hafnium.identity.dto.CustomerResponse;
import dev.hafnium.identity.dto.UpdateCustomerRequest;
import dev.hafnium.identity.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for customer management.
 *
 * <p>
 * Provides endpoints for customer CRUD operations per the OpenAPI
 * specification.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer.
     *
     * @param request The customer creation request
     * @return The created customer
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    /**
     * Lists customers with optional filtering.
     *
     * @param status   Optional status filter
     * @param riskTier Optional risk tier filter
     * @param cursor   Optional pagination cursor
     * @param limit    Maximum number of results (default 50, max 100)
     * @return Paginated list of customers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<PagedResponse<List<CustomerResponse>>> listCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(name = "risk_tier", required = false) RiskTier riskTier,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int limit) {

        // Enforce maximum limit
        limit = Math.min(limit, 100);

        // Parse cursor for offset-based pagination
        int offset = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                offset = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                // Invalid cursor, start from beginning
            }
        }

        Page<CustomerResponse> page = customerService.listCustomers(status, riskTier,
                PageRequest.of(offset / limit, limit));

        String nextCursor = page.hasNext() ? String.valueOf(offset + limit) : null;

        return ResponseEntity.ok(
                PagedResponse.of(page.getContent(), nextCursor, page.hasNext()));
    }

    /**
     * Gets a customer by ID.
     *
     * @param customerId The customer identifier
     * @return The customer details
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID customerId) {
        CustomerResponse customer = customerService.getCustomer(customerId);
        return ResponseEntity.ok(customer);
    }

    /**
     * Updates a customer.
     *
     * @param customerId The customer identifier
     * @param request    The update request
     * @return The updated customer
     */
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse customer = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(customer);
    }
}
