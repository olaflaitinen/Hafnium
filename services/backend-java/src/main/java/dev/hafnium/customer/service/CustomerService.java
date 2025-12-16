package dev.hafnium.customer.service;

import dev.hafnium.customer.domain.Customer;
import dev.hafnium.customer.dto.CreateCustomerRequest;
import dev.hafnium.customer.dto.CustomerResponse;
import dev.hafnium.customer.dto.UpdateCustomerRequest;
import dev.hafnium.customer.repository.CustomerRepository;
import dev.hafnium.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for customer management operations.
 *
 * <p>
 * Handles business logic for customer CRUD operations and coordinates with
 * other services for
 * KYC workflows and event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    /**
     * Create a new customer.
     *
     * @param request Creation request
     * @return Created customer response
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with external ID: {}", request.getExternalId());

        Customer customer = Customer.builder()
                .externalId(request.getExternalId())
                .customerType(
                        Customer.CustomerType.valueOf(
                                request.getCustomerType().toUpperCase()))
                .status(Customer.CustomerStatus.PENDING)
                .build();

        customer = customerRepository.save(customer);

        log.info("Customer created with ID: {}", customer.getCustomerId());

        return customerMapper.toResponse(customer);
    }

    /**
     * Get customer by ID.
     *
     * @param customerId Customer UUID
     * @return Customer response
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        Customer customer = findCustomerOrThrow(customerId);
        return customerMapper.toResponse(customer);
    }

    /**
     * List customers with filters and pagination.
     *
     * @param status   Status filter
     * @param riskTier Risk tier filter
     * @param pageable Pagination parameters
     * @return Page of customer responses
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(
            String status, String riskTier, Pageable pageable) {

        Page<Customer> customers;

        if (status != null && riskTier != null) {
            customers = customerRepository.findByStatusAndRiskTier(
                    Customer.CustomerStatus.valueOf(status.toUpperCase()),
                    Customer.RiskTier.valueOf(riskTier.toUpperCase()),
                    pageable);
        } else if (status != null) {
            customers = customerRepository.findByStatus(
                    Customer.CustomerStatus.valueOf(status.toUpperCase()),
                    pageable);
        } else if (riskTier != null) {
            customers = customerRepository.findByRiskTier(
                    Customer.RiskTier.valueOf(riskTier.toUpperCase()),
                    pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        return customers.map(customerMapper::toResponse);
    }

    /**
     * Update customer.
     *
     * @param customerId Customer UUID
     * @param request    Update request
     * @return Updated customer response
     */
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: {}", customerId);

        Customer customer = findCustomerOrThrow(customerId);

        if (request.getStatus() != null) {
            customer.setStatus(
                    Customer.CustomerStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getRiskTier() != null) {
            customer.setRiskTier(
                    Customer.RiskTier.valueOf(request.getRiskTier().toUpperCase()));
        }

        customer = customerRepository.save(customer);

        log.info("Customer updated: {}", customerId);

        return customerMapper.toResponse(customer);
    }

    /**
     * Delete customer (soft delete).
     *
     * @param customerId Customer UUID
     */
    @Transactional
    public void deleteCustomer(UUID customerId) {
        log.info("Deleting customer: {}", customerId);

        Customer customer = findCustomerOrThrow(customerId);
        customer.setStatus(Customer.CustomerStatus.REJECTED);
        customerRepository.save(customer);

        log.info("Customer deleted: {}", customerId);
    }

    private Customer findCustomerOrThrow(UUID customerId) {
        return customerRepository
                .findById(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer", customerId.toString()));
    }
}
