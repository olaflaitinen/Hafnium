package dev.hafnium.screening.service;

import dev.hafnium.common.kafka.EventPublisher;
import dev.hafnium.common.kafka.Topics;
import dev.hafnium.common.security.TenantContext;
import dev.hafnium.screening.domain.SanctionsEntity;
import dev.hafnium.screening.domain.ScreeningMatch;
import dev.hafnium.screening.dto.ScreeningRequest;
import dev.hafnium.screening.dto.ScreeningResponse;
import dev.hafnium.screening.repository.SanctionsEntityRepository;
import dev.hafnium.screening.repository.ScreeningMatchRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Screening service for sanctions and PEP matching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final SanctionsEntityRepository sanctionsRepository;
    private final ScreeningMatchRepository matchRepository;
    private final EventPublisher eventPublisher;

    @Value("${hafnium.screening.fuzzy-threshold:0.85}")
    private double fuzzyThreshold;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    /**
     * Screens a name against sanctions and PEP lists.
     */
    @Transactional
    public ScreeningResponse screenName(ScreeningRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID requestId = UUID.randomUUID();

        String normalizedQuery = normalizeName(request.name());

        log.info("Screening name: query={}, requestId={}", request.name(), requestId);

        // Find potential matches
        List<SanctionsEntity> candidates = sanctionsRepository.findActiveByTenantId(tenantId);
        List<ScreeningMatch> matches = new ArrayList<>();

        for (SanctionsEntity entity : candidates) {
            double score = calculateMatchScore(normalizedQuery, entity.getNameNormalized());

            if (score >= fuzzyThreshold) {
                ScreeningMatch match = ScreeningMatch.builder()
                        .tenantId(tenantId)
                        .requestId(requestId)
                        .entity(entity)
                        .queryName(request.name())
                        .matchedName(entity.getPrimaryName())
                        .matchScore(BigDecimal.valueOf(score))
                        .matchType(determineMatchType(score))
                        .status(ScreeningMatch.MatchStatus.PENDING_REVIEW)
                        .build();

                matches.add(matchRepository.save(match));
            }
        }

        log.info("Screening completed: requestId={}, matchCount={}", requestId, matches.size());

        // Publish event
        if (!matches.isEmpty()) {
            eventPublisher.publish(
                    Topics.SCREENING_COMPLETED,
                    "screening.match.completed",
                    "1.0.0",
                    requestId.toString(),
                    new ScreeningCompletedEvent(requestId, request.name(), matches.size()));
        }

        return new ScreeningResponse(
                requestId,
                request.name(),
                matches.size(),
                matches.stream().map(this::toMatchDto).toList());
    }

    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double calculateMatchScore(String query, String target) {
        int distance = levenshtein.apply(query, target);
        int maxLen = Math.max(query.length(), target.length());
        if (maxLen == 0)
            return 1.0;
        return 1.0 - ((double) distance / maxLen);
    }

    private ScreeningMatch.MatchType determineMatchType(double score) {
        if (score >= 0.99)
            return ScreeningMatch.MatchType.EXACT;
        if (score >= 0.90)
            return ScreeningMatch.MatchType.FUZZY;
        return ScreeningMatch.MatchType.PHONETIC;
    }

    private ScreeningResponse.MatchDto toMatchDto(ScreeningMatch match) {
        return new ScreeningResponse.MatchDto(
                match.getId(),
                match.getMatchedName(),
                match.getMatchScore().doubleValue(),
                match.getMatchType().name(),
                match.getEntity().getListSource(),
                match.getEntity().getListType().name());
    }

    private record ScreeningCompletedEvent(UUID requestId, String queryName, int matchCount) {
    }
}
