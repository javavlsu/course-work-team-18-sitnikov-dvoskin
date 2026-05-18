package ru.cinema.dto.playlist;

import ru.cinema.dto.content.ContentListItem;
import ru.cinema.model.PlaylistContent;

import java.time.LocalDateTime;

public record PlaylistItemResponse(
        ContentListItem content,
        LocalDateTime addedAt,
        Integer sortOrder
) {
    public static PlaylistItemResponse of(PlaylistContent pc) {
        return new PlaylistItemResponse(
                ContentListItem.of(pc.getContent()),
                pc.getAddedAt(),
                pc.getSortOrder()
        );
    }
}
