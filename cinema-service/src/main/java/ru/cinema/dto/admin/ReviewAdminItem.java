package ru.cinema.dto.admin;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.dto.common.ContentRef;
import ru.cinema.model.Review;
import ru.cinema.model.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewAdminItem(
        Long id,
        String title,
        ReviewStatus status,
        String moderationReason,
        Integer viewCount,
        Integer likeCount,
        LocalDateTime createdAt,
        AuthorRef author,
        ContentRef content
) {
    public static ReviewAdminItem of(Review r) {
        return new ReviewAdminItem(
                r.getId(),
                r.getTitle(),
                r.getStatus(),
                r.getModerationReason(),
                r.getViewCount(),
                r.getLikeCount(),
                r.getCreatedAt(),
                AuthorRef.of(r.getUser()),
                ContentRef.of(r.getContent())
        );
    }
}
