package dev.hafnium.customer.api;

import dev.hafnium.customer.dto.CreateCustomerRequest;
import dev.hafnium.customer.dto.CustomerResponse;
import dev.hafnium.customer.dto.UpdateCustomerRequest;
import dev.hafnium.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer management operations.
 *
 * <p>
 * Provides endpoints for CRUD operations on customers and initiating KYC
 * workflows.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create a new customer.
     *
     * @param request Customer creation request
     * @return Created customer
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer by ID.
     *
     * @param customerId Customer UUID
     * @return Customer details
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    /**
     * List customers with pagination.
     *
     * @param status   Optional status filter
     * @param riskTier Optional risk tier filter
     * @param pageable Pagination parameters
     * @return Page of customers
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskTier,
            Pageable pageable) {
        return ResponseEntity.ok(customerService.listCustomers(status, riskTier, pageable));
    }

    /**
     * Update customer.
     *
     * @param customerId Customer UUID
     * @param request    Update request
     * @return Updated customer
     */
    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    /**
     * Delete customer (soft delete).
     *
     * @param customerId Customer UUID
     * @return No content
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
