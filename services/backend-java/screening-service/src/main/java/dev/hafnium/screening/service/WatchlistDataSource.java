package dev.hafnium.screening.service;

import dev.hafnium.screening.engine.FuzzyMatchingEngine.WatchlistEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Data source for watchlist entries.
 *
 * <p>
 * Provides sample watchlist data for development and testing purposes. In
 * production, this
 * would be replaced with integration to actual sanctions list providers.
 *
 * <p>
 * NOTICE: This implementation uses synthetic data only. No actual sanctions or
 * PEP data is
 * included in this repository.
 */
@Component
public class WatchlistDataSource {

    private final List<WatchlistEntry> entries = new ArrayList<>();

    public WatchlistDataSource() {
        initializeSampleData();
    }

    /**
     * Gets all watchlist entries.
     *
     * @return List of all watchlist entries
     */
    public List<WatchlistEntry> getAllEntries() {
        return List.copyOf(entries);
    }

    /**
     * Gets entries from a specific list.
     *
     * @param listName The list name
     * @return List of entries from that list
     */
    public List<WatchlistEntry> getEntriesByList(String listName) {
        return entries.stream().filter(e -> e.listName().equals(listName)).toList();
    }

    /**
     * Adds a new entry to the watchlist.
     *
     * @param entry The entry to add
     */
    public void addEntry(WatchlistEntry entry) {
        entries.add(entry);
    }

    /**
     * Clears all entries.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Gets the count of entries.
     *
     * @return Number of entries
     */
    public int getEntryCount() {
        return entries.size();
    }

    /** Initializes sample data for development. */
    private void initializeSampleData() {
        // Sample synthetic entries for testing only
        // These are fictional names and do not represent real individuals

        // Sample OFAC-style entries
        entries.add(
                new WatchlistEntry(
                        "SAMPLE-001",
                        "John Sample Testperson",
                        "SAMPLE_SANCTIONS",
                        Map.of("type", "individual", "program", "SAMPLE_PROGRAM")));

        entries.add(
                new WatchlistEntry(
                        "SAMPLE-002",
                        "Test Organization LLC",
                        "SAMPLE_SANCTIONS",
                        Map.of("type", "entity", "program", "SAMPLE_PROGRAM")));

        entries.add(
                new WatchlistEntry(
                        "SAMPLE-003",
                        "Jane Doe Example",
                        "SAMPLE_PEP",
                        Map.of("type", "pep", "position", "Sample Position")));

        entries.add(
                new WatchlistEntry(
                        "SAMPLE-004",
                        "Example Trading Company",
                        "SAMPLE_SANCTIONS",
                        Map.of("type", "entity", "program", "SAMPLE_PROGRAM")));

        entries.add(
                new WatchlistEntry(
                        "SAMPLE-005",
                        "Sample Development Bank",
                        "SAMPLE_FINANCIAL",
                        Map.of("type", "financial_institution", "country", "XX")));
    }
}
