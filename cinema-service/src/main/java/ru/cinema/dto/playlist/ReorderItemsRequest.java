package ru.cinema.dto.playlist;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReorderItemsRequest(
        @NotNull List<Item> items
) {
    public record Item(@NotNull Long contentId, @NotNull Integer sortOrder) {}
}
