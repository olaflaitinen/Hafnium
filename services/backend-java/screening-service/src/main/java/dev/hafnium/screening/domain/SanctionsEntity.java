package dev.hafnium.screening.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Sanctions entity from watchlists (OFAC, EU, UN, etc.).
 */
@Entity
@Table(name = "sanctions_entities", schema = "screening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "list_source", nullable = false)
    private String listSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false)
    private ListType listType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "primary_name", nullable = false, length = 500)
    private String primaryName;

    @Column(name = "name_normalized", nullable = false, length = 500)
    private String nameNormalized;

    @Column(columnDefinition = "jsonb")
    private String aliases;

    @Column(columnDefinition = "jsonb")
    private String identifiers;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(columnDefinition = "jsonb")
    private String countries;

    @Column(columnDefinition = "jsonb")
    private String programs;

    @Column(columnDefinition = "text")
    private String remarks;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum ListType {
        SANCTIONS,
        PEP,
        ADVERSE_MEDIA,
        ENFORCEMENT
    }

    public enum EntityType {
        INDIVIDUAL,
        ORGANIZATION,
        VESSEL,
        AIRCRAFT
    }
}
