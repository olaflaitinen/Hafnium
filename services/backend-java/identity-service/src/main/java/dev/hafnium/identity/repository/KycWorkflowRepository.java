package dev.hafnium.identity.repository;

import dev.hafnium.identity.domain.KycWorkflow;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for KYC Workflow entities.
 */
@Repository
public interface KycWorkflowRepository extends JpaRepository<KycWorkflow, UUID> {

    /**
     * Find workflow by ID and tenant.
     */
    @Query("SELECT w FROM KycWorkflow w WHERE w.id = :id AND w.tenantId = :tenantId")
    Optional<KycWorkflow> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    /**
     * Find the most recent workflow for a customer.
     */
    @Query("SELECT w FROM KycWorkflow w WHERE w.customer.id = :customerId AND w.tenantId = :tenantId ORDER BY w.createdAt DESC LIMIT 1")
    Optional<KycWorkflow> findLatestByCustomerIdAndTenantId(
            @Param("customerId") UUID customerId, @Param("tenantId") UUID tenantId);
}
