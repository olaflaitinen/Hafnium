package dev.hafnium.screening.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

/**
 * Fuzzy matching engine for sanctions and PEP screening.
 *
 * <p>
 * Implements multiple string similarity algorithms to identify potential
 * matches between query
 * names and watchlist entries. The engine combines Levenshtein distance for
 * edit-based matching
 * and Jaro-Winkler similarity for phonetic-like matching.
 */
@Component
public class FuzzyMatchingEngine {

    private static final double DEFAULT_THRESHOLD = 0.85;
    private static final int MAX_LEVENSHTEIN_DISTANCE = 3;

    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final LevenshteinDistance levenshtein = new LevenshteinDistance(MAX_LEVENSHTEIN_DISTANCE);

    /**
     * Finds matches for a given name against a list of watchlist entries.
     *
     * @param queryName        The name to search for
     * @param watchlistEntries The list of watchlist entries to match against
     * @param threshold        The minimum similarity threshold (0.0 to 1.0)
     * @return List of matching entries with scores
     */
    public List<MatchResult> findMatches(
            String queryName, List<WatchlistEntry> watchlistEntries, double threshold) {
        if (queryName == null || queryName.isBlank()) {
            return List.of();
        }

        String normalizedQuery = normalize(queryName);
        List<MatchResult> matches = new ArrayList<>();

        for (WatchlistEntry entry : watchlistEntries) {
            double score = calculateSimilarity(normalizedQuery, normalize(entry.name()));

            if (score >= threshold) {
                matches.add(
                        new MatchResult(
                                entry.id(),
                                entry.name(),
                                entry.listName(),
                                score,
                                determineReasonCodes(normalizedQuery, normalize(entry.name()), score),
                                entry.metadata()));
            }
        }

        // Sort by score descending
        matches.sort((a, b) -> Double.compare(b.score(), a.score()));

        return matches;
    }

    /**
     * Finds matches using the default threshold.
     *
     * @param queryName        The name to search for
     * @param watchlistEntries The list of watchlist entries
     * @return List of matching entries
     */
    public List<MatchResult> findMatches(String queryName, List<WatchlistEntry> watchlistEntries) {
        return findMatches(queryName, watchlistEntries, DEFAULT_THRESHOLD);
    }

    /**
     * Calculates the combined similarity score between two strings.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return Similarity score between 0.0 and 1.0
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        // Exact match
        if (s1.equals(s2)) {
            return 1.0;
        }

        // Jaro-Winkler similarity (good for short strings and typos)
        double jwScore = jaroWinkler.apply(s1, s2);

        // Levenshtein distance normalized to similarity
        Integer distance = levenshtein.apply(s1, s2);
        double levScore = distance != null && distance != -1
                ? 1.0 - (double) distance / Math.max(s1.length(), s2.length())
                : 0.0;

        // Also check for substring containment
        double substringScore = 0.0;
        if (s1.contains(s2) || s2.contains(s1)) {
            substringScore = 0.9; // High score for substring match
        }

        // Return the maximum of all similarity measures
        return Math.max(Math.max(jwScore, levScore), substringScore);
    }

    /**
     * Normalizes a string for comparison.
     *
     * @param input The input string
     * @return Normalized string
     */
    private String normalize(String input) {
        if (input == null) {
            return "";
        }

        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    /**
     * Determines reason codes for a match.
     *
     * @param query   The query string
     * @param matched The matched string
     * @param score   The similarity score
     * @return List of reason codes
     */
    private List<String> determineReasonCodes(String query, String matched, double score) {
        List<String> reasons = new ArrayList<>();

        if (query.equals(matched)) {
            reasons.add("EXACT_NAME_MATCH");
        } else if (query.contains(matched) || matched.contains(query)) {
            reasons.add("PARTIAL_NAME_MATCH");
        } else if (score >= 0.95) {
            reasons.add("HIGH_SIMILARITY_MATCH");
        } else if (score >= 0.85) {
            reasons.add("FUZZY_NAME_MATCH");
        }

        return reasons;
    }

    /**
     * Represents a watchlist entry to match against.
     *
     * @param id       Entry identifier
     * @param name     The name in the watchlist
     * @param listName The source list name (e.g., "OFAC", "UN", "EU")
     * @param metadata Additional entry data
     */
    public record WatchlistEntry(
            String id, String name, String listName, Map<String, Object> metadata) {
    }

    /**
     * Represents a match result.
     *
     * @param entryId     The matched entry ID
     * @param matchedName The matched name
     * @param listName    The source list name
     * @param score       The similarity score
     * @param reasonCodes List of reason codes explaining the match
     * @param metadata    Additional entry data
     */
    public record MatchResult(
            String entryId,
            String matchedName,
            String listName,
            double score,
            List<String> reasonCodes,
            Map<String, Object> metadata) {
    }
}
