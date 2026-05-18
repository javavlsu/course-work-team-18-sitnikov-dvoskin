package ru.cinema.dto.content;

import ru.cinema.dto.tag.TagResponse;
import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentType;

import java.math.BigDecimal;
import java.util.List;

public record ContentListItem(
        Long id,
        String title,
        String originalTitle,
        Integer releaseYear,
        String posterUrl,
        BigDecimal averageRating,
        ContentType contentType,
        String country,
        List<TagResponse> tags
) {
    public static ContentListItem of(Content c) {
        if (c == null) return null;
        List<TagResponse> tags = c.getTags() == null ? List.of()
                : c.getTags().stream().map(TagResponse::of).toList();
        return new ContentListItem(
                c.getId(),
                c.getTitle(),
                c.getOriginalTitle(),
                c.getReleaseYear(),
                c.getPosterUrl(),
                c.getAverageRating(),
                c.getContentType(),
                c.getCountry(),
                tags
        );
    }
}
