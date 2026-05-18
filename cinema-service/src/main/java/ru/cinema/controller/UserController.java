package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.playlist.PlaylistListItem;
import ru.cinema.dto.review.ReviewListItem;
import ru.cinema.dto.user.PublicUserResponse;
import ru.cinema.dto.user.UpdateUserRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Профиль пользователя")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.toResponse(CurrentUser.currentUser(), true);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return userService.updateProfile(CurrentUser.requireUserId(), req);
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
