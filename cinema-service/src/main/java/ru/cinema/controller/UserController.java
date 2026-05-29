package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.playlist.PlaylistListItem;
import ru.cinema.dto.rating.RatingResponse;
import ru.cinema.dto.review.ReviewListItem;
import ru.cinema.dto.user.PublicUserResponse;
import ru.cinema.dto.user.UpdateUserRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.rating.RatingService;
import ru.cinema.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Профиль пользователя")
public class UserController {

    private final UserService userService;
    private final RatingService ratingService;

    public UserController(UserService userService, RatingService ratingService) {
        this.userService = userService;
        this.ratingService = ratingService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.toResponse(CurrentUser.currentUser(), true);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return userService.updateProfile(CurrentUser.requireUserId(), req);
    }

    /** GET /api/v1/users/me/reviews — заявлен в API map Этапа 8. */
    @GetMapping("/me/reviews")
    public PageResponse<ReviewListItem> myReviews(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(userService.getUserReviewsById(CurrentUser.requireUserId(), pageable).map(ReviewListItem::of));
    }

    /** GET /api/v1/users/me/playlists. */
    @GetMapping("/me/playlists")
    public List<PlaylistListItem> myPlaylists() {
        return userService.getUserPlaylistsById(CurrentUser.requireUserId()).stream()
                .map(PlaylistListItem::of)
                .toList();
    }

    /** GET /api/v1/users/me/ratings — все оценки текущего юзера. */
    @GetMapping("/me/ratings")
    public List<RatingResponse> myRatings() {
        return ratingService.listByUser(CurrentUser.requireUserId());
    }

    @GetMapping("/{username}")
    public PublicUserResponse byUsername(@PathVariable String username) {
        return userService.toPublicResponse(userService.getByUsername(username));
    }

    @GetMapping("/{username}/reviews")
    public PageResponse<ReviewListItem> reviewsByUser(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(userService.getUserReviews(username, pageable).map(ReviewListItem::of));
    }

    @GetMapping("/{username}/playlists")
    public List<PlaylistListItem> playlistsByUser(@PathVariable String username) {
        return userService.getUserPlaylists(username).stream()
                .map(PlaylistListItem::of)
                .toList();
    }
}
