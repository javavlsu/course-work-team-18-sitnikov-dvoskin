package ru.cinema.dto.playlist;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.model.Playlist;

import java.time.LocalDateTime;

public record PlaylistListItem(
        Long id,
        String title,
        String coverImageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        AuthorRef owner,
        int itemsCount
) {
    public static PlaylistListItem of(Playlist p) {
        return new PlaylistListItem(
                p.getId(),
                p.getTitle(),
                p.getCoverImageUrl(),
                p.getIsPublic(),
                p.getCreatedAt(),
                AuthorRef.of(p.getUser()),
                p.getPlaylistContents() == null ? 0 : p.getPlaylistContents().size()
        );
    }
}
