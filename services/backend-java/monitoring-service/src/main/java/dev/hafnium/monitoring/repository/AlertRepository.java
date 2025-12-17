package dev.hafnium.monitoring.repository;

import dev.hafnium.monitoring.domain.Alert;
import dev.hafnium.monitoring.domain.Alert.AlertStatus;
import dev.hafnium.monitoring.domain.Alert.Severity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Alert entities.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    /**
     * Finds an alert by tenant and alert ID.
     *
     * @param tenantId The tenant identifier
     * @param alertId  The alert identifier
     * @return The alert if found
     */
    Optional<Alert> findByTenantIdAndAlertId(UUID tenantId, UUID alertId);

    /**
     * Finds alerts with optional filtering.
     *
     * @param tenantId The tenant identifier
     * @param status   Optional status filter
     * @param severity Optional severity filter
     * @param pageable Pagination parameters
     * @return Page of alerts
     */
    @Query("""
            SELECT a FROM Alert a
            WHERE a.tenantId = :tenantId
            AND (:status IS NULL OR a.status = :status)
            AND (:severity IS NULL OR a.severity = :severity)
            ORDER BY a.createdAt DESC
            """)
    Page<Alert> findByTenantIdWithFilters(
            @Param("tenantId") UUID tenantId,
            @Param("status") AlertStatus status,
            @Param("severity") Severity severity,
            Pageable pageable);
}
