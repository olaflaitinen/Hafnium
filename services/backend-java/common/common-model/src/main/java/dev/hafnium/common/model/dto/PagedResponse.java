package dev.hafnium.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cursor-based pagination response wrapper.
 *
 * <p>
 * Provides a consistent pagination structure across all list endpoints. Uses
 * cursor-based
 * pagination for efficient traversal of large datasets.
 *
 * @param <T> The type of data items in the response
 */
public record PagedResponse<T>(
        @JsonProperty("data") T data,
        @JsonProperty("pagination") CursorPagination pagination) {

    /**
     * Creates a paged response with pagination metadata.
     *
     * @param data       The data items
     * @param nextCursor The cursor for the next page (null if no more pages)
     * @param hasMore    Whether there are more items after this page
     * @param <T>        The data type
     * @return A new PagedResponse
     */
    public static <T> PagedResponse<T> of(T data, String nextCursor, boolean hasMore) {
        return new PagedResponse<>(data, new CursorPagination(nextCursor, hasMore));
    }

    /**
     * Creates a paged response for a single page with no more items.
     *
     * @param data The data items
     * @param <T>  The data type
     * @return A new PagedResponse with hasMore=false
     */
    public static <T> PagedResponse<T> single(T data) {
        return new PagedResponse<>(data, new CursorPagination(null, false));
    }

    /**
     * Cursor-based pagination metadata.
     *
     * @param nextCursor The opaque cursor for the next page
     * @param hasMore    Whether more items exist beyond this page
     */
    public record CursorPagination(
            @JsonProperty("next_cursor") String nextCursor, @JsonProperty("has_more") boolean hasMore) {
    }
}
