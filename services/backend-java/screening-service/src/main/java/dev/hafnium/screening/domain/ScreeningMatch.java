package dev.hafnium.screening.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Screening match domain entity.
 *
 * <p>
 * Represents a match result from screening against a sanctions or PEP list.
 */
@Entity
@Table(name = "screening_matches")
public class ScreeningMatch {

    @Id
    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "list_name", nullable = false)
    private String listName;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal matchScore;

    @Column(name = "matched_name")
    private String matchedName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_data", columnDefinition = "jsonb")
    private Map<String, Object> matchedData;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition")
    private MatchDisposition disposition = MatchDisposition.PENDING;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ScreeningMatch() {
        this.matchId = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public ScreeningMatch(
            UUID requestId, String listName, BigDecimal matchScore, String matchedName,
            Map<String, Object> matchedData) {
        this();
        this.requestId = requestId;
        this.listName = listName;
        this.matchScore = matchScore;
        this.matchedName = matchedName;
        this.matchedData = matchedData;
    }

    // Getters and setters

    public UUID getMatchId() {
        return matchId;
    }

    public void setMatchId(UUID matchId) {
        this.matchId = matchId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchedName() {
        return matchedName;
    }

    public void setMatchedName(String matchedName) {
        this.matchedName = matchedName;
    }

    public Map<String, Object> getMatchedData() {
        return matchedData;
    }

    public void setMatchedData(Map<String, Object> matchedData) {
        this.matchedData = matchedData;
    }

    public MatchDisposition getDisposition() {
        return disposition;
    }

    public void setDisposition(MatchDisposition disposition) {
        this.disposition = disposition;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** Match disposition statuses. */
    public enum MatchDisposition {
        PENDING,
        TRUE_POSITIVE,
        FALSE_POSITIVE,
        ESCALATED
    }
}
