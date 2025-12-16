package dev.hafnium.vault.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Access log entity (immutable audit trail).
 */
@Entity
@Table(name = "access_log", schema = "vault")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id")
    private Token token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operation operation;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    private String reason;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    public enum Operation {
        TOKENIZE,
        DETOKENIZE,
        DELETE
    }
}
