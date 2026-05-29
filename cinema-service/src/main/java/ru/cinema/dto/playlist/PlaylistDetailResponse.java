package ru.cinema.dto.playlist;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.model.Playlist;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PlaylistDetailResponse(
        Long id,
        String title,
        String description,
        String coverImageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AuthorRef owner,
        int itemsCount,
        List<PlaylistItemResponse> items,
        double ratingAverage,
        long ratingsCount
) {
    public static PlaylistDetailResponse of(Playlist p, double ratingAverage, long ratingsCount) {
        List<PlaylistItemResponse> items = p.getPlaylistContents() == null ? List.of()
                : p.getPlaylistContents().stream()
                    .sorted(Comparator.comparing(pc -> pc.getSortOrder() == null ? Integer.MAX_VALUE : pc.getSortOrder()))
                    .map(PlaylistItemResponse::of)
                    .toList();
        return new PlaylistDetailResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getCoverImageUrl(),
                p.getIsPublic(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                AuthorRef.of(p.getUser()),
                items.size(),
                items,
                ratingAverage,
                ratingsCount
        );
    }
}
