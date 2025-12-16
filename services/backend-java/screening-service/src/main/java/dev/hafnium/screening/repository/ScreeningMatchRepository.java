package dev.hafnium.screening.repository;

import dev.hafnium.screening.domain.ScreeningMatch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for screening matches.
 */
@Repository
public interface ScreeningMatchRepository extends JpaRepository<ScreeningMatch, UUID> {

    List<ScreeningMatch> findByRequestId(UUID requestId);

    List<ScreeningMatch> findByTenantIdAndStatus(UUID tenantId, ScreeningMatch.MatchStatus status);
}
