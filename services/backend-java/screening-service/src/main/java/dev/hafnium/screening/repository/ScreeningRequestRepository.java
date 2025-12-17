package dev.hafnium.screening.repository;

import dev.hafnium.screening.domain.ScreeningRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ScreeningRequest entities.
 */
@Repository
public interface ScreeningRequestRepository extends JpaRepository<ScreeningRequest, UUID> {

    /**
     * Finds a screening request by tenant and request ID.
     *
     * @param tenantId  The tenant identifier
     * @param requestId The request identifier
     * @return The screening request if found
     */
    Optional<ScreeningRequest> findByTenantIdAndRequestId(UUID tenantId, UUID requestId);
}
