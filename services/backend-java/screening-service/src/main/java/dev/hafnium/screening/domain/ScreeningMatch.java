package dev.hafnium.screening.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Screening match result.
 */
@Entity
@Table(name = "screening_matches", schema = "screening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    private SanctionsEntity entity;

    @Column(name = "query_name", nullable = false, length = 500)
    private String queryName;

    @Column(name = "matched_name", nullable = false, length = 500)
    private String matchedName;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal matchScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.PENDING_REVIEW;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Enumerated(EnumType.STRING)
    private Resolution resolution;

    @Column(name = "resolution_notes", columnDefinition = "text")
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public enum MatchType {
        EXACT,
        FUZZY,
        PHONETIC,
        ALIAS
    }

    public enum MatchStatus {
        PENDING_REVIEW,
        CONFIRMED,
        FALSE_POSITIVE,
        ESCALATED
    }

    public enum Resolution {
        TRUE_MATCH,
        FALSE_POSITIVE,
        REQUIRES_ESCALATION
    }
}
