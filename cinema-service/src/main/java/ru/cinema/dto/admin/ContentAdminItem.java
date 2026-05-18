package ru.cinema.dto.admin;

import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContentAdminItem(
        Long id,
        String title,
        ContentType contentType,
        ContentStatus status,
        BigDecimal averageRating,
        Integer releaseYear,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContentAdminItem of(Content c) {
        return new ContentAdminItem(
                c.getId(),
                c.getTitle(),
                c.getContentType(),
                c.getStatus(),
                c.getAverageRating(),
                c.getReleaseYear(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
