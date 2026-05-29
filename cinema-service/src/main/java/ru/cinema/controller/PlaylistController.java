package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.playlist.*;
import ru.cinema.model.Playlist;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.playlist.PlaylistService;
import ru.cinema.service.rating.PlaylistRatingService;

@RestController
@RequestMapping("/api/v1/playlists")
@Tag(name = "Playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistRatingService ratingService;

    public PlaylistController(PlaylistService playlistService, PlaylistRatingService ratingService) {
        this.playlistService = playlistService;
        this.ratingService = ratingService;
    }

    private PlaylistDetailResponse toDetail(Playlist p) {
        return PlaylistDetailResponse.of(p, ratingService.averageFor(p.getId()), ratingService.countFor(p.getId()));
    }

    @GetMapping
    public PageResponse<PlaylistListItem> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "true") Boolean isPublic,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(playlistService.list(userId, isPublic, pageable).map(PlaylistListItem::of));
    }

    @GetMapping("/{id}")
    public PlaylistDetailResponse byId(@PathVariable Long id) {
        return toDetail(playlistService.getVisible(id, CurrentUser.currentUserId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistDetailResponse create(@Valid @RequestBody CreatePlaylistRequest req) {
        return toDetail(playlistService.create(CurrentUser.requireUserId(), req));
    }

    @PatchMapping("/{id}")
    public PlaylistDetailResponse update(@PathVariable Long id,
                                         @Valid @RequestBody UpdatePlaylistRequest req) {
        return toDetail(playlistService.update(id, CurrentUser.requireUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.delete(id, CurrentUser.requireUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public PlaylistDetailResponse addItem(@PathVariable Long id,
                                          @Valid @RequestBody AddPlaylistItemRequest req) {
        return toDetail(playlistService.addItem(id, CurrentUser.requireUserId(), req));
    }

    @DeleteMapping("/{id}/items/{contentId}")
    public PlaylistDetailResponse removeItem(@PathVariable Long id, @PathVariable Long contentId) {
        return toDetail(playlistService.removeItem(id, CurrentUser.requireUserId(), contentId));
    }

    @PatchMapping("/{id}/items/reorder")
    public PlaylistDetailResponse reorder(@PathVariable Long id,
                                          @Valid @RequestBody ReorderItemsRequest req) {
        return toDetail(playlistService.reorder(id, CurrentUser.requireUserId(), req));
    }
}
