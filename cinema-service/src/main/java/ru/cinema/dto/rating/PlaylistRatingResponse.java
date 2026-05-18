package ru.cinema.dto.rating;

import ru.cinema.model.PlaylistRating;

public record PlaylistRatingResponse(Long id, Long playlistId, Integer value, Double average, long count) {
    public static PlaylistRatingResponse of(PlaylistRating pr, double avg, long count) {
        return new PlaylistRatingResponse(pr.getId(), pr.getPlaylist().getId(), pr.getValue(), avg, count);
    }
}
