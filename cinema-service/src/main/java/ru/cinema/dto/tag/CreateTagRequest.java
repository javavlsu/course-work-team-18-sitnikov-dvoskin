package ru.cinema.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 50) String slug,
        @Size(max = 255) String description
) {}
