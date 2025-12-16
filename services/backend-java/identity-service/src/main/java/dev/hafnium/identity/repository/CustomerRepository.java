package dev.hafnium.identity.repository;

import dev.hafnium.identity.domain.Customer;
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
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find customer by ID and tenant, excluding soft-deleted.
     */
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.tenantId = :tenantId AND c.deletedAt IS NULL")
    Optional<Customer> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    /**
     * Find customer by external ID and tenant.
     */
    @Query("SELECT c FROM Customer c WHERE c.externalId = :externalId AND c.tenantId = :tenantId AND c.deletedAt IS NULL")
    Optional<Customer> findByExternalIdAndTenantId(
            @Param("externalId") String externalId, @Param("tenantId") UUID tenantId);

    /**
     * Check if external ID exists for tenant.
     */
    boolean existsByExternalIdAndTenantId(String externalId, UUID tenantId);

    /**
     * Find all customers for a tenant with pagination.
     */
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.deletedAt IS NULL")
    Page<Customer> findAllByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    /**
     * Find customers by status for a tenant.
     */
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status = :status AND c.deletedAt IS NULL")
    Page<Customer> findByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") Customer.CustomerStatus status,
            Pageable pageable);
}
