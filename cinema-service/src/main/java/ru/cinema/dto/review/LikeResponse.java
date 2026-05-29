package ru.cinema.dto.review;

/**
 * Ответ на toggle лайка: новое состояние + актуальный счётчик.
 */
public record LikeResponse(boolean liked, long likeCount) {}
