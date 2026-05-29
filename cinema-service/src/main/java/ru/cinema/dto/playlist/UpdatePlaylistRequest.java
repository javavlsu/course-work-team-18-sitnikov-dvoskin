package ru.cinema.dto.playlist;

import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(
        @Size(max = 100) String title,
        String description,
        @Size(max = 500) String coverImageUrl,
        Boolean isPublic
) {}
