package dev.hafnium.signals.repository;

import dev.hafnium.signals.domain.Signal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalRepository extends JpaRepository<Signal, UUID> {
    Page<Signal> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Signal> findByTenantIdAndStatus(UUID tenantId, Signal.SignalStatus status, Pageable pageable);
}
