package ru.cinema.dto.genre;

import ru.cinema.model.Genre;

public record GenreResponse(Long id, String name, String slug, String description) {
    public static GenreResponse of(Genre g) {
        return new GenreResponse(g.getId(), g.getName(), g.getSlug(), g.getDescription());
    }
}
