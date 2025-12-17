package dev.hafnium.screening.repository;

import dev.hafnium.screening.domain.ScreeningMatch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ScreeningMatch entities.
 */
@Repository
public interface ScreeningMatchRepository extends JpaRepository<ScreeningMatch, UUID> {

    /**
     * Finds all matches for a screening request.
     *
     * @param requestId The screening request identifier
     * @return List of matches
     */
    List<ScreeningMatch> findByRequestId(UUID requestId);
}
