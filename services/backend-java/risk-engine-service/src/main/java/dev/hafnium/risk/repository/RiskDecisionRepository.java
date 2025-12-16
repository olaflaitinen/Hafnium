package dev.hafnium.risk.repository;

import dev.hafnium.risk.domain.RiskDecision;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for risk decisions.
 */
@Repository
public interface RiskDecisionRepository extends JpaRepository<RiskDecision, UUID> {

    /**
     * Find the most recent decision for an entity.
     */
    @Query("SELECT d FROM RiskDecision d WHERE d.entityType = :entityType AND d.entityId = :entityId AND d.tenantId = :tenantId ORDER BY d.computedAt DESC LIMIT 1")
    Optional<RiskDecision> findLatestByEntityAndTenant(
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            @Param("tenantId") UUID tenantId);

    /**
     * Find decision by ID and tenant.
     */
    Optional<RiskDecision> findByIdAndTenantId(UUID id, UUID tenantId);
}
