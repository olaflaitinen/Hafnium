package dev.hafnium.screening.service;

import dev.hafnium.common.kafka.KafkaEventPublisher;
import dev.hafnium.common.model.event.EventType;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.screening.domain.ScreeningMatch;
import dev.hafnium.screening.domain.ScreeningRequest;
import dev.hafnium.screening.domain.ScreeningRequest.EntityType;
import dev.hafnium.screening.domain.ScreeningRequest.ScreeningStatus;
import dev.hafnium.screening.dto.ScreeningMatchRequest;
import dev.hafnium.screening.dto.ScreeningMatchResponse;
import dev.hafnium.screening.engine.FuzzyMatchingEngine;
import dev.hafnium.screening.engine.FuzzyMatchingEngine.MatchResult;
import dev.hafnium.screening.engine.FuzzyMatchingEngine.WatchlistEntry;
import dev.hafnium.screening.repository.ScreeningMatchRepository;
import dev.hafnium.screening.repository.ScreeningRequestRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for screening operations.
 *
 * <p>
 * Handles screening requests against sanctions and PEP lists using fuzzy
 * matching.
 */
@Service
@Transactional
public class ScreeningService {

    private static final Logger LOG = LoggerFactory.getLogger(ScreeningService.class);

    private final ScreeningRequestRepository requestRepository;
    private final ScreeningMatchRepository matchRepository;
    private final FuzzyMatchingEngine matchingEngine;
    private final WatchlistDataSource watchlistDataSource;
    private final KafkaEventPublisher eventPublisher;

    public ScreeningService(
            ScreeningRequestRepository requestRepository,
            ScreeningMatchRepository matchRepository,
            FuzzyMatchingEngine matchingEngine,
            WatchlistDataSource watchlistDataSource,
            KafkaEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.matchRepository = matchRepository;
        this.matchingEngine = matchingEngine;
        this.watchlistDataSource = watchlistDataSource;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Performs screening against sanctions and PEP lists.
     *
     * @param request The screening request
     * @return The screening response with matches
     */
    public ScreeningMatchResponse performScreening(ScreeningMatchRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        String actorId = TenantContext.requireActorId();

        // Create screening request record
        ScreeningRequest screeningRequest = new ScreeningRequest(
                tenantId,
                request.entityType() != null ? request.entityType() : EntityType.CUSTOMER,
                request.entityId(),
                Map.of(
                        "name", request.name(),
                        "date_of_birth", request.dateOfBirth() != null ? request.dateOfBirth() : "",
                        "country", request.country() != null ? request.country() : ""));

        screeningRequest.setStatus(ScreeningStatus.IN_PROGRESS);
        screeningRequest = requestRepository.save(screeningRequest);

        LOG.info(
                "Starting screening request {} for entity {} in tenant {}",
                screeningRequest.getRequestId(),
                request.entityId(),
                tenantId);

        // Get watchlist entries
        List<WatchlistEntry> watchlistEntries = watchlistDataSource.getAllEntries();

        // Perform matching
        double threshold = request.threshold() != null ? request.threshold() : 0.85;
        List<MatchResult> matches = matchingEngine.findMatches(request.name(), watchlistEntries, threshold);

        // Save matches
        List<ScreeningMatch> savedMatches = matches.stream()
                .map(
                        m -> new ScreeningMatch(
                                screeningRequest.getRequestId(),
                                m.listName(),
                                BigDecimal.valueOf(m.score()),
                                m.matchedName(),
                                Map.of(
                                        "entry_id", m.entryId(),
                                        "reason_codes", m.reasonCodes(),
                                        "metadata", m.metadata() != null ? m.metadata() : Map.of())))
                .toList();

        matchRepository.saveAll(savedMatches);

        // Update screening request
        screeningRequest.setStatus(ScreeningStatus.COMPLETED);
        screeningRequest.setMatchCount(matches.size());
        screeningRequest.setCompletedAt(Instant.now());
        screeningRequest.setResult(
                Map.of(
                        "total_matches", matches.size(),
                        "high_risk_matches", matches.stream().filter(m -> m.score() >= 0.95).count()));

        requestRepository.save(screeningRequest);

        LOG.info(
                "Completed screening request {} with {} matches",
                screeningRequest.getRequestId(),
                matches.size());

        // Emit event
        eventPublisher.publish(
                EventType.SCREENING_COMPLETED,
                tenantId,
                actorId,
                TenantContext.getOrCreateTraceId(),
                Map.of(
                        "request_id", screeningRequest.getRequestId(),
                        "entity_id", request.entityId(),
                        "match_count", matches.size(),
                        "has_high_risk_matches", matches.stream().anyMatch(m -> m.score() >= 0.95)));

        return new ScreeningMatchResponse(
                screeningRequest.getRequestId(),
                screeningRequest.getStatus().name().toLowerCase(),
                matches.size(),
                matches.stream()
                        .map(
                                m -> new ScreeningMatchResponse.Match(
                                        m.matchedName(), m.listName(), m.score(), m.reasonCodes()))
                        .toList(),
                screeningRequest.getCreatedAt());
    }
}
