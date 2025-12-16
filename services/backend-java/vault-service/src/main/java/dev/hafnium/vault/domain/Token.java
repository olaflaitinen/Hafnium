package dev.hafnium.vault.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Token entity representing encrypted sensitive data.
 */
@Entity
@Table(name = "tokens", schema = "vault")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "encrypted_value", nullable = false)
    private byte[] encryptedValue;

    @Column(nullable = false)
    private byte[] iv;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "access_count", nullable = false)
    @Builder.Default
    private Integer accessCount = 0;
}
