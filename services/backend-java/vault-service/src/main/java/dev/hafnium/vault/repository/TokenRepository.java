package dev.hafnium.vault.repository;

import dev.hafnium.vault.domain.Token;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByTenantIdAndToken(UUID tenantId, String token);
}
