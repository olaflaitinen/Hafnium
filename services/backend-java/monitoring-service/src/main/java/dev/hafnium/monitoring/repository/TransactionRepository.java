package dev.hafnium.monitoring.repository;

import dev.hafnium.monitoring.domain.Transaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        /**
         * Finds a transaction by tenant and transaction ID.
         *
         * @param tenantId The tenant identifier
         * @param txnId    The transaction identifier
         * @return The transaction if found
         */
        Optional<Transaction> findByTenantIdAndTxnId(UUID tenantId, UUID txnId);
}
