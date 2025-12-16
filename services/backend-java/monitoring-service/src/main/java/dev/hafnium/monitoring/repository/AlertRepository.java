package dev.hafnium.monitoring.repository;

import dev.hafnium.monitoring.domain.Alert;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for alerts.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Page<Alert> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Alert> findByTenantIdAndStatus(UUID tenantId, Alert.AlertStatus status, Pageable pageable);

    List<Alert> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId);

    long countByTenantIdAndStatus(UUID tenantId, Alert.AlertStatus status);
}
