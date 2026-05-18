package ru.cinema.dto.playlist;

import jakarta.validation.constraints.NotNull;

public record AddPlaylistItemRequest(
        @NotNull Long contentId,
        Integer sortOrder
) {}
