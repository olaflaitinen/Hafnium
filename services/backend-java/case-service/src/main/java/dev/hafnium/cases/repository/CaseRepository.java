package dev.hafnium.cases.repository;

import dev.hafnium.cases.domain.Case;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for cases.
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {

    Optional<Case> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Case> findByTenantIdAndCaseNumber(UUID tenantId, String caseNumber);

    Page<Case> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Case> findByTenantIdAndStatus(UUID tenantId, Case.CaseStatus status, Pageable pageable);

    long countByTenantIdAndStatus(UUID tenantId, Case.CaseStatus status);
}
