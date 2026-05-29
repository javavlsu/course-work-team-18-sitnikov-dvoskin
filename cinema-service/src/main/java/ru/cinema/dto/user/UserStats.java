package ru.cinema.dto.user;

/**
 * Агрегированная статистика по пользователю (для публичного и приватного профиля).
 */
public record UserStats(
        long reviewsCount,
        long playlistsCount,
        long ratingsCount,
        Double averageRatingGiven
) {}
