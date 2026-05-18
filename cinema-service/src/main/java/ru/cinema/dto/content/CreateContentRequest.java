package ru.cinema.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.cinema.model.enums.ContentType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Универсальный запрос на создание контента (фильм или сериал).
 * Поля movie-specific (duration/budget/boxOffice) и series-specific (totalSeasons/totalEpisodes/isFinished)
 * заполняются опционально и применяются только при соответствующем type.
 */
public record CreateContentRequest(
        @NotNull(message = "type обязателен (MOVIE или SERIES)")
        ContentType type,

        @NotBlank(message = "title обязателен")
        @Size(max = 255)
        String title,

        @Size(max = 255) String originalTitle,
        String description,
        Integer releaseYear,
        @Size(max = 500) String posterUrl,
        @Size(max = 100) String country,
        @Size(max = 50) String language,
        @Size(max = 20) String imdbId,
        @Size(max = 20) String kinopoiskId,

        List<Long> tagIds,

        // movie
        Integer duration,
        BigDecimal budget,
        BigDecimal boxOffice,

        // series
        Integer totalSeasons,
        Integer totalEpisodes,
        Boolean isFinished
) {}
