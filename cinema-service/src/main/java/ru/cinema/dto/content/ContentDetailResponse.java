package ru.cinema.dto.content;

import ru.cinema.dto.tag.TagResponse;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.Series;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;

import java.math.BigDecimal;
import java.util.List;

public record ContentDetailResponse(
        Long id,
        String title,
        String originalTitle,
        Integer releaseYear,
        String posterUrl,
        BigDecimal averageRating,
        ContentType contentType,
        String country,
        List<TagResponse> tags,

        String description,
        String language,
        String imdbId,
        String kinopoiskId,
        ContentStatus status,
        long totalRatings,
        long totalReviews,
        long totalComments,

        // movie-specific
        Integer duration,
        BigDecimal budget,
        BigDecimal boxOffice,

        // series-specific
        Integer totalSeasons,
        Integer totalEpisodes,
        Boolean isFinished
) {

    public static ContentDetailResponse of(Content c, long totalRatings, long totalReviews, long totalComments) {
        if (c == null) return null;
        List<TagResponse> tags = c.getTags() == null ? List.of()
                : c.getTags().stream().map(TagResponse::of).toList();

        Integer duration = null;
        BigDecimal budget = null;
        BigDecimal boxOffice = null;
        Integer totalSeasons = null;
        Integer totalEpisodes = null;
        Boolean isFinished = null;

        if (c instanceof Movie m) {
            duration = m.getDuration();
            budget = m.getBudget();
            boxOffice = m.getBoxOffice();
        } else if (c instanceof Series s) {
            totalSeasons = s.getTotalSeasons();
            totalEpisodes = s.getTotalEpisodes();
            isFinished = s.getIsFinished();
        }

        return new ContentDetailResponse(
                c.getId(),
                c.getTitle(),
                c.getOriginalTitle(),
                c.getReleaseYear(),
                c.getPosterUrl(),
                c.getAverageRating(),
                c.getContentType(),
                c.getCountry(),
                tags,
                c.getDescription(),
                c.getLanguage(),
                c.getImdbId(),
                c.getKinopoiskId(),
                c.getStatus(),
                totalRatings,
                totalReviews,
                totalComments,
                duration, budget, boxOffice,
                totalSeasons, totalEpisodes, isFinished
        );
    }
}
