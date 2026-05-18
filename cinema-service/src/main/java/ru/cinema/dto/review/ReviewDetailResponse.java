package ru.cinema.dto.review;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.dto.common.ContentRef;
import ru.cinema.model.Review;
import ru.cinema.model.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewDetailResponse(
        Long id,
        String title,
        String text,
        Integer ratingValue,
        Integer viewCount,
        Integer likeCount,
        ReviewStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AuthorRef author,
        ContentRef content
) {
    public static ReviewDetailResponse of(Review r) {
        return new ReviewDetailResponse(
                r.getId(),
                r.getTitle(),
                r.getText(),
                r.getRatingValue(),
                r.getViewCount(),
                r.getLikeCount(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                AuthorRef.of(r.getUser()),
                ContentRef.of(r.getContent())
        );
    }
}
