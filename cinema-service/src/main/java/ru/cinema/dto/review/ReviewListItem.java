package ru.cinema.dto.review;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.dto.common.ContentRef;
import ru.cinema.model.Review;
import ru.cinema.model.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewListItem(
        Long id,
        String title,
        String excerpt,
        Integer ratingValue,
        Integer viewCount,
        Integer likeCount,
        ReviewStatus status,
        LocalDateTime createdAt,
        AuthorRef author,
        ContentRef content
) {
    private static final int EXCERPT_LIMIT = 280;

    public static ReviewListItem of(Review r) {
        return new ReviewListItem(
                r.getId(),
                r.getTitle(),
                excerptOf(r.getText()),
                r.getRatingValue(),
                r.getViewCount(),
                r.getLikeCount(),
                r.getStatus(),
                r.getCreatedAt(),
                AuthorRef.of(r.getUser()),
                ContentRef.of(r.getContent())
        );
    }

    /**
     * Превью текста рецензии для лент: схлопывает переносы в пробелы,
     * обрезает до {@value #EXCERPT_LIMIT} символов по границе слова + многоточие.
     */
    private static String excerptOf(String text) {
        if (text == null) return null;
        String flat = text.replaceAll("\\s+", " ").trim();
        if (flat.length() <= EXCERPT_LIMIT) return flat;
        String cut = flat.substring(0, EXCERPT_LIMIT);
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > EXCERPT_LIMIT - 40) cut = cut.substring(0, lastSpace);
        return cut + "…";
    }
}
