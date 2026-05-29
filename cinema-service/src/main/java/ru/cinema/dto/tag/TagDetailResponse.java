package ru.cinema.dto.tag;

import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.model.Tag;

public record TagDetailResponse(
        Long id,
        String name,
        String slug,
        String description,
        Integer usageCount,
        PageResponse<ContentListItem> content
) {
    public static TagDetailResponse of(Tag tag, PageResponse<ContentListItem> content) {
        return new TagDetailResponse(
                tag.getId(),
                tag.getName(),
                tag.getSlug(),
                tag.getDescription(),
                tag.getUsageCount(),
                content
        );
    }
}
