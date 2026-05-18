package ru.cinema.dto.rating;

import ru.cinema.model.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
        Integer value,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RatingResponse of(Rating r) {
        return new RatingResponse(r.getValue(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
