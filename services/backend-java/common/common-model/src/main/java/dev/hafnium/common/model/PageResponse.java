package dev.hafnium.common.model;

import lombok.Builder;
import lombok.Value;

/**
 * Cursor-based pagination response wrapper.
 *
 * @param <T> the type of items in the page
 */
@Value
@Builder
public class PageResponse<T> {
    java.util.List<T> data;
    Pagination pagination;

    @Value
    @Builder
    public static class Pagination {
        String nextCursor;
        boolean hasMore;
    }

    public static <T> PageResponse<T> of(java.util.List<T> data, String nextCursor, boolean hasMore) {
        return PageResponse.<T>builder()
                .data(data)
                .pagination(Pagination.builder().nextCursor(nextCursor).hasMore(hasMore).build())
                .build();
    }

    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .data(java.util.List.of())
                .pagination(Pagination.builder().nextCursor(null).hasMore(false).build())
                .build();
    }
}
