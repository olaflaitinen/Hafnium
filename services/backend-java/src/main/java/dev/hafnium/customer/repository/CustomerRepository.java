package dev.hafnium.customer.repository;

import dev.hafnium.customer.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Customer entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find customer by external ID and tenant.
     *
     * @param externalId External customer identifier
     * @param tenantId   Tenant UUID
     * @return Optional customer
     */
    Optional<Customer> findByExternalIdAndTenantId(String externalId, UUID tenantId);

    /**
     * Find customers by status with pagination.
     *
     * @param status   Customer status
     * @param pageable Pagination parameters
     * @return Page of customers
     */
    Page<Customer> findByStatus(Customer.CustomerStatus status, Pageable pageable);

    /**
     * Find customers by risk tier with pagination.
     *
     * @param riskTier Risk tier
     * @param pageable Pagination parameters
     * @return Page of customers
     */
    Page<Customer> findByRiskTier(Customer.RiskTier riskTier, Pageable pageable);

    /**
     * Find customers by status and risk tier with pagination.
     *
     * @param status   Customer status
     * @param riskTier Risk tier
     * @param pageable Pagination parameters
     * @return Page of customers
     */
    Page<Customer> findByStatusAndRiskTier(
            Customer.CustomerStatus status,
            Customer.RiskTier riskTier,
            Pageable pageable);

    /**
     * Count customers by status.
     *
     * @param status Customer status
     * @return Count
     */
    long countByStatus(Customer.CustomerStatus status);
}
