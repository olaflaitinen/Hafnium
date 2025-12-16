package dev.hafnium.cases.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Case event for state machine audit trail.
 */
@Entity
@Table(name = "case_events", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case investigationCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state")
    private Case.CaseStatus fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state")
    private Case.CaseStatus toState;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    public enum EventType {
        CREATED,
        STATUS_CHANGED,
        ASSIGNED,
        PRIORITY_CHANGED,
        EVIDENCE_ADDED,
        NOTE_ADDED,
        ESCALATED,
        CLOSED
    }
}
