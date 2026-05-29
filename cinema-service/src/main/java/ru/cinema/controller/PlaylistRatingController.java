package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.rating.PlaylistRatingResponse;
import ru.cinema.dto.rating.RateRequest;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.rating.PlaylistRatingService;

/**
 * Эндпоинты оценки подборки (CollectionRating из Этапа 3, шкала 1–5).
 */
@RestController
@RequestMapping("/api/v1/playlists/{playlistId}/rating")
@Tag(name = "Playlist Ratings")
public class PlaylistRatingController {

    private final PlaylistRatingService service;

    public PlaylistRatingController(PlaylistRatingService service) { this.service = service; }

    @GetMapping("/me")
    public ResponseEntity<PlaylistRatingResponse> my(@PathVariable Long playlistId) {
        return service.myRating(CurrentUser.requireUserId(), playlistId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public PlaylistRatingResponse rate(@PathVariable Long playlistId,
                                       @Valid @RequestBody RateRequest req) {
        return service.rate(CurrentUser.requireUserId(), playlistId, req.value());
    }

    @PostMapping
    public PlaylistRatingResponse ratePost(@PathVariable Long playlistId,
                                           @Valid @RequestBody RateRequest req) {
        return service.rate(CurrentUser.requireUserId(), playlistId, req.value());
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@PathVariable Long playlistId) {
        service.remove(CurrentUser.requireUserId(), playlistId);
        return ResponseEntity.noContent().build();
    }
}
