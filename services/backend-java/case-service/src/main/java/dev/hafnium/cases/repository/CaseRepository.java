package dev.hafnium.cases.repository;

import dev.hafnium.cases.domain.Case;
import dev.hafnium.cases.domain.Case.CaseStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Case entities.
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {

    Optional<Case> findByTenantIdAndCaseId(UUID tenantId, UUID caseId);

    @Query("""
            SELECT c FROM Case c
            WHERE c.tenantId = :tenantId
            AND (:status IS NULL OR c.status = :status)
            ORDER BY c.createdAt DESC
            """)
    Page<Case> findByTenantIdWithFilters(
            @Param("tenantId") UUID tenantId,
            @Param("status") CaseStatus status,
            Pageable pageable);
}
