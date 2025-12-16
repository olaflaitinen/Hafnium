package dev.hafnium.identity.service;

import dev.hafnium.common.kafka.EventPublisher;
import dev.hafnium.common.kafka.Topics;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.identity.domain.Customer;
import dev.hafnium.identity.dto.CreateCustomerRequest;
import dev.hafnium.identity.dto.CustomerResponse;
import dev.hafnium.identity.dto.UpdateCustomerRequest;
import dev.hafnium.identity.repository.CustomerRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for customer operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    /**
     * Creates a new customer.
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        if (customerRepository.existsByExternalIdAndTenantId(request.externalId(), tenantId)) {
            throw new IllegalArgumentException(
                    "Customer with external ID already exists: " + request.externalId());
        }

        Customer customer = Customer.builder()
                .tenantId(tenantId)
                .externalId(request.externalId())
                .customerType(
                        request.customerType() != null
                                ? Customer.CustomerType.valueOf(request.customerType().toUpperCase())
                                : Customer.CustomerType.INDIVIDUAL)
                .status(Customer.CustomerStatus.PENDING)
                .metadata(request.metadata())
                .build();

        customer = customerRepository.save(customer);

        log.info("Created customer: id={}, externalId={}", customer.getId(), customer.getExternalId());

        return toResponse(customer);
    }

    /**
     * Gets a customer by ID.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        UUID tenantId = TenantContext.requireTenantId();

        Customer customer = customerRepository
                .findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        return toResponse(customer);
    }

    /**
     * Lists customers with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(String status, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();

        Page<Customer> customers;
        if (status != null && !status.isBlank()) {
            Customer.CustomerStatus customerStatus = Customer.CustomerStatus.valueOf(status.toUpperCase());
            customers = customerRepository.findByTenantIdAndStatus(tenantId, customerStatus, pageable);
        } else {
            customers = customerRepository.findAllByTenantId(tenantId, pageable);
        }

        return customers.map(this::toResponse);
    }

    /**
     * Updates a customer.
     */
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, UpdateCustomerRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Customer customer = customerRepository
                .findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        if (request.status() != null) {
            customer.setStatus(Customer.CustomerStatus.valueOf(request.status().toUpperCase()));
        }
        if (request.riskTier() != null) {
            customer.setRiskTier(Customer.RiskTier.valueOf(request.riskTier().toUpperCase()));
        }
        if (request.metadata() != null) {
            customer.setMetadata(request.metadata());
        }

        customer = customerRepository.save(customer);

        log.info("Updated customer: id={}", customer.getId());

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getExternalId(),
                customer.getCustomerType().name().toLowerCase(),
                customer.getStatus().name().toLowerCase(),
                customer.getRiskTier() != null ? customer.getRiskTier().name().toLowerCase() : null,
                customer.getMetadata(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
