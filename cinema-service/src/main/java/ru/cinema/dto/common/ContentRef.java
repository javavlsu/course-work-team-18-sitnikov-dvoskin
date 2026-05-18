package ru.cinema.dto.common;

import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentType;

/**
 * Краткая ссылка на контент (используется в DTO рецензий, элементов подборок).
 */
public record ContentRef(Long id, String title, String posterUrl, ContentType contentType) {

    public static ContentRef of(Content content) {
        if (content == null) return null;
        return new ContentRef(
                content.getId(),
                content.getTitle(),
                content.getPosterUrl(),
                content.getContentType()
        );
    }
}
