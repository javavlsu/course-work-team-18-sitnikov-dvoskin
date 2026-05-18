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
        Boolean hasLiked,
        ReviewStatus status,
        String moderationReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AuthorRef author,
        ContentRef content
) {
    public static ReviewDetailResponse of(Review r) {
        return of(r, null);
    }

    public static ReviewDetailResponse of(Review r, Boolean hasLiked) {
        return new ReviewDetailResponse(
                r.getId(),
                r.getTitle(),
                r.getText(),
                r.getRatingValue(),
                r.getViewCount(),
                r.getLikeCount(),
                hasLiked,
                r.getStatus(),
                r.getModerationReason(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                AuthorRef.of(r.getUser()),
                ContentRef.of(r.getContent())
        );
    }
}
