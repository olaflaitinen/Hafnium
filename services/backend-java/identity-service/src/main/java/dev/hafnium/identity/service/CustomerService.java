package dev.hafnium.identity.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.common.web.ResourceConflictException;
import dev.hafnium.common.web.ResourceNotFoundException;
import dev.hafnium.identity.domain.Customer;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.Customer.CustomerType;
import dev.hafnium.identity.domain.Customer.RiskTier;
import dev.hafnium.identity.dto.CreateCustomerRequest;
import dev.hafnium.identity.dto.CustomerResponse;
import dev.hafnium.identity.dto.UpdateCustomerRequest;
import dev.hafnium.identity.repository.CustomerRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for customer management operations.
 *
 * <p>
 * Handles customer creation, updates, and lifecycle management with proper
 * tenant isolation and
 * event emission.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final KafkaEventPublisher eventPublisher;

    public CustomerService(
            CustomerRepository customerRepository, KafkaEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new customer.
     *
     * @param request The customer creation request
     * @return The created customer response
     * @throws ResourceConflictException if a customer with the external ID already
     *                                   exists
     */
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        // Check for duplicate external ID
        if (customerRepository.existsByTenantIdAndExternalId(tenantId, request.externalId())) {
            throw ResourceConflictException.duplicate("Customer", "external_id", request.externalId());
        }

        // Create customer
        Customer customer = new Customer(tenantId, request.externalId());
        customer.setCustomerType(
                request.customerType() != null ? request.customerType() : CustomerType.INDIVIDUAL);
        customer.setMetadata(request.metadata());

        customer = customerRepository.save(customer);

        LOG.info("Created customer {} for tenant {}", customer.getCustomerId(), tenantId);

        // Emit event
        eventPublisher.publish(
                EventType.CUSTOMER_CREATED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "customer_id", customer.getCustomerId(),
                        "external_id", customer.getExternalId(),
                        "customer_type", customer.getCustomerType().name()));

        return toResponse(customer);
    }

    /**
     * Gets a customer by ID.
     *
     * @param customerId The customer identifier
     * @return The customer response
     * @throws ResourceNotFoundException if the customer is not found
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        UUID tenantId = TenantContext.requireTenantId();

        Customer customer = customerRepository
                .findByTenantIdAndCustomerId(tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));

        return toResponse(customer);
    }

    /**
     * Lists customers with optional filtering.
     *
     * @param status   Optional status filter
     * @param riskTier Optional risk tier filter
     * @param pageable Pagination parameters
     * @return Page of customer responses
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(
            CustomerStatus status, RiskTier riskTier, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();

        return customerRepository
                .findByTenantIdWithFilters(tenantId, status, riskTier, pageable)
                .map(this::toResponse);
    }

    /**
     * Updates a customer.
     *
     * @param customerId The customer identifier
     * @param request    The update request
     * @return The updated customer response
     * @throws ResourceNotFoundException if the customer is not found
     */
    public CustomerResponse updateCustomer(UUID customerId, UpdateCustomerRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Customer customer = customerRepository
                .findByTenantIdAndCustomerId(tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));

        // Update fields
        if (request.status() != null) {
            customer.setStatus(request.status());
        }
        if (request.riskTier() != null) {
            customer.setRiskTier(request.riskTier());
        }
        if (request.metadata() != null) {
            customer.setMetadata(request.metadata());
        }

        customer.touch();
        customer = customerRepository.save(customer);

        LOG.info("Updated customer {} for tenant {}", customerId, tenantId);

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getExternalId(),
                customer.getCustomerType(),
                customer.getStatus(),
                customer.getRiskTier(),
                customer.getMetadata(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
