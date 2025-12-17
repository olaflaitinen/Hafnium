package dev.hafnium.identity.repository;

import dev.hafnium.identity.domain.KycWorkflow;
import dev.hafnium.identity.domain.KycWorkflow.WorkflowStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for KycWorkflow entities.
 *
 * <p>
 * All queries are scoped to a specific tenant for multi-tenant isolation.
 */
@Repository
public interface KycWorkflowRepository extends JpaRepository<KycWorkflow, UUID> {

    /**
     * Finds a workflow by tenant and workflow ID.
     *
     * @param tenantId   The tenant identifier
     * @param workflowId The workflow identifier
     * @return The workflow if found
     */
    Optional<KycWorkflow> findByTenantIdAndWorkflowId(UUID tenantId, UUID workflowId);

    /**
     * Finds the latest workflow for a customer.
     *
     * @param tenantId   The tenant identifier
     * @param customerId The customer identifier
     * @return The latest workflow if found
     */
    @Query("""
            SELECT w FROM KycWorkflow w
            WHERE w.tenantId = :tenantId
            AND w.customerId = :customerId
            ORDER BY w.createdAt DESC
            LIMIT 1
            """)
    Optional<KycWorkflow> findLatestByTenantIdAndCustomerId(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);

    /**
     * Checks if there is an active workflow for a customer.
     *
     * @param tenantId   The tenant identifier
     * @param customerId The customer identifier
     * @return true if an active workflow exists
     */
    @Query("""
            SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END
            FROM KycWorkflow w
            WHERE w.tenantId = :tenantId
            AND w.customerId = :customerId
            AND w.status NOT IN ('APPROVED', 'REJECTED')
            """)
    boolean existsActiveWorkflow(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId);
}
