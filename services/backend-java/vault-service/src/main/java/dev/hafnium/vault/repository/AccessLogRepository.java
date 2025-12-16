package dev.hafnium.vault.repository;

import dev.hafnium.vault.domain.AccessLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
}
