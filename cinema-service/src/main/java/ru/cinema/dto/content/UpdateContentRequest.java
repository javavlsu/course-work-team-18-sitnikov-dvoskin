package ru.cinema.dto.content;

import jakarta.validation.constraints.Size;
import ru.cinema.model.enums.ContentStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Запрос на частичное обновление контента. Все поля nullable.
 * Поля movie/series применяются, если контент соответствующего типа.
 */
public record UpdateContentRequest(
        @Size(max = 255) String title,
        @Size(max = 255) String originalTitle,
        String description,
        Integer releaseYear,
        @Size(max = 500) String posterUrl,
        @Size(max = 100) String country,
        @Size(max = 50) String language,
        @Size(max = 20) String imdbId,
        @Size(max = 20) String kinopoiskId,
        ContentStatus status,
        List<Long> tagIds,

        Integer duration,
        BigDecimal budget,
        BigDecimal boxOffice,

        Integer totalSeasons,
        Integer totalEpisodes,
        Boolean isFinished
) {}
