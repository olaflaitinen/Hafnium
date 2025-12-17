package dev.hafnium.screening.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for screening match operations.
 */
public record ScreeningMatchResponse(
        @JsonProperty("request_id") UUID requestId,
        @JsonProperty("status") String status,
        @JsonProperty("match_count") int matchCount,
        @JsonProperty("matches") List<Match> matches,
        @JsonProperty("created_at") Instant createdAt) {

    /**
     * Individual match result.
     */
    public record Match(
            @JsonProperty("matched_name") String matchedName,
            @JsonProperty("list_name") String listName,
            @JsonProperty("score") double score,
            @JsonProperty("reason_codes") List<String> reasonCodes) {
    }
}
