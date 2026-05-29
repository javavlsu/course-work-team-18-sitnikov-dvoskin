package ru.cinema.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlaylistRequest(
        @NotBlank @Size(max = 100) String title,
        String description,
        @Size(max = 500) String coverImageUrl,
        Boolean isPublic
) {}
