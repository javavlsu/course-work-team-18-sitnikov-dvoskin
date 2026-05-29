package ru.cinema.dto.tag;

import ru.cinema.model.Tag;

/**
 * DTO тега для возврата клиенту.
 */
public record TagResponse(Long id, String name, String slug, String description, Integer usageCount) {

    public static TagResponse of(Tag tag) {
        if (tag == null) return null;
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getDescription(), tag.getUsageCount());
    }
}
