package dev.hafnium.identity.repository;

import dev.hafnium.identity.domain.Customer;
import dev.hafnium.identity.domain.Customer.CustomerStatus;
import dev.hafnium.identity.domain.Customer.RiskTier;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer entities.
 *
 * <p>
 * All queries are scoped to a specific tenant for multi-tenant isolation.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

        /**
         * Finds a customer by tenant and customer ID.
         *
         * @param tenantId   The tenant identifier
         * @param customerId The customer identifier
         * @return The customer if found
         */
        Optional<Customer> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId);

        /**
         * Finds a customer by tenant and external ID.
         *
         * @param tenantId   The tenant identifier
         * @param externalId The external customer identifier
         * @return The customer if found
         */
        Optional<Customer> findByTenantIdAndExternalId(UUID tenantId, String externalId);

        /**
         * Checks if a customer exists with the given external ID in a tenant.
         *
         * @param tenantId   The tenant identifier
         * @param externalId The external customer identifier
         * @return true if the customer exists
         */
        boolean existsByTenantIdAndExternalId(UUID tenantId, String externalId);

        /**
         * Finds all customers in a tenant with optional filtering.
         *
         * @param tenantId The tenant identifier
         * @param status   Optional status filter
         * @param riskTier Optional risk tier filter
         * @param pageable Pagination parameters
         * @return Page of customers
         */
        @Query("""
                        SELECT c FROM Customer c
                        WHERE c.tenantId = :tenantId
                        AND (:status IS NULL OR c.status = :status)
                        AND (:riskTier IS NULL OR c.riskTier = :riskTier)
                        ORDER BY c.createdAt DESC
                        """)
        Page<Customer> findByTenantIdWithFilters(
                        @Param("tenantId") UUID tenantId,
                        @Param("status") CustomerStatus status,
                        @Param("riskTier") RiskTier riskTier,
                        Pageable pageable);
}
