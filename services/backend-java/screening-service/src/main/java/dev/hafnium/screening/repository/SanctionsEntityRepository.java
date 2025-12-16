package dev.hafnium.screening.repository;

import dev.hafnium.screening.domain.SanctionsEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for sanctions entities.
 */
@Repository
public interface SanctionsEntityRepository extends JpaRepository<SanctionsEntity, UUID> {

    @Query("SELECT e FROM SanctionsEntity e WHERE e.tenantId = :tenantId AND e.active = true")
    List<SanctionsEntity> findActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT e FROM SanctionsEntity e WHERE e.tenantId = :tenantId AND e.listSource = :listSource AND e.active = true")
    List<SanctionsEntity> findByTenantIdAndListSource(
            @Param("tenantId") UUID tenantId, @Param("listSource") String listSource);
}
