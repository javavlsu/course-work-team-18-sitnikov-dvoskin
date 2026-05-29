package ru.cinema.dto.admin;

import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.model.enums.UserRole;

import java.util.Map;

public record DashboardStatsResponse(
        UserStats users,
        ContentStats content,
        ReviewStats reviews,
        CommentStats comments,
        RatingStats ratings
) {
    public record UserStats(long total, long active, Map<UserRole, Long> byRole) {}
    public record ContentStats(long total, Map<ContentStatus, Long> byStatus, Map<ContentType, Long> byType) {}
    public record ReviewStats(long total, Map<ReviewStatus, Long> byStatus, long totalLikes, long totalViews) {}
    public record CommentStats(long total) {}
    public record RatingStats(long total, Double averageOverall) {}
}
