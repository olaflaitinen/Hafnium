package dev.hafnium.cases.repository;

import dev.hafnium.cases.domain.CaseEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for case events.
 */
@Repository
public interface CaseEventRepository extends JpaRepository<CaseEvent, UUID> {

    @Query("SELECT e FROM CaseEvent e WHERE e.investigationCase.id = :caseId ORDER BY e.occurredAt DESC")
    List<CaseEvent> findByCaseIdOrderByOccurredAtDesc(@Param("caseId") UUID caseId);
}
