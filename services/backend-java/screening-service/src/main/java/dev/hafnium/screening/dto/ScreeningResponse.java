package dev.hafnium.screening.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/**
 * Screening response DTO.
 */
public record ScreeningResponse(
        @JsonProperty("request_id") UUID requestId,
        @JsonProperty("query_name") String queryName,
        @JsonProperty("match_count") int matchCount,
        List<MatchDto> matches) {

    public record MatchDto(
            @JsonProperty("match_id") UUID matchId,
            @JsonProperty("matched_name") String matchedName,
            double score,
            @JsonProperty("match_type") String matchType,
            @JsonProperty("list_source") String listSource,
            @JsonProperty("list_type") String listType) {
    }
}
