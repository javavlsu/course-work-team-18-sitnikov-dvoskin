package ru.cinema.dto.playlist;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.model.Playlist;
import ru.cinema.model.PlaylistContent;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistListItem(
        Long id,
        String title,
        String coverImageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        AuthorRef owner,
        int itemsCount,
        /** Постеры первых 4 фильмов — для mosaic-превью карточки. */
        List<String> previewPosters
) {
    public static PlaylistListItem of(Playlist p) {
        List<PlaylistContent> contents = p.getPlaylistContents();
        int total = contents == null ? 0 : contents.size();
        List<String> previews = contents == null ? List.of() : contents.stream()
                .sorted((a, b) -> Integer.compare(
                        a.getSortOrder() == null ? 0 : a.getSortOrder(),
                        b.getSortOrder() == null ? 0 : b.getSortOrder()))
                .map(pc -> pc.getContent() == null ? null : pc.getContent().getPosterUrl())
                .filter(u -> u != null && !u.isBlank())
                .limit(4)
                .toList();
        return new PlaylistListItem(
                p.getId(),
                p.getTitle(),
                p.getCoverImageUrl(),
                p.getIsPublic(),
                p.getCreatedAt(),
                AuthorRef.of(p.getUser()),
                total,
                previews
        );
    }
}
