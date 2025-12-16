package dev.hafnium.monitoring.repository;

import dev.hafnium.monitoring.domain.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for transactions.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTenantIdAndExternalId(UUID tenantId, String externalId);

    Page<Transaction> findByTenantId(UUID tenantId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.tenantId = :tenantId AND t.customerId = :customerId AND t.transactionTimestamp >= :since")
    List<Transaction> findByCustomerSince(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId,
            @Param("since") Instant since);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.tenantId = :tenantId AND t.customerId = :customerId AND t.transactionTimestamp >= :since")
    long countByCustomerSince(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId,
            @Param("since") Instant since);
}
