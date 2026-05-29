package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.user.PublicUserResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.social.UserFollowService;
import ru.cinema.service.user.UserService;

import java.util.List;

/**
 * Endpoints для подписок: POST/DELETE /users/{username}/follow + GET followers|following.
 */
@RestController
@RequestMapping("/api/v1/users/{username}")
@Tag(name = "User · Follow")
public class UserFollowController {

    private final UserFollowService followService;
    private final UserService userService;

    public UserFollowController(UserFollowService followService, UserService userService) {
        this.followService = followService;
        this.userService = userService;
    }

    @PostMapping("/follow")
    public ResponseEntity<Void> follow(@PathVariable String username) {
        followService.follow(CurrentUser.requireUserId(), username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow")
    public ResponseEntity<Void> unfollow(@PathVariable String username) {
        followService.unfollow(CurrentUser.requireUserId(), username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public List<PublicUserResponse> followers(@PathVariable String username) {
        return followService.listFollowers(username).stream()
                .map(userService::toPublicResponse).toList();
    }

    @GetMapping("/following")
    public List<PublicUserResponse> following(@PathVariable String username) {
        return followService.listFollowing(username).stream()
                .map(userService::toPublicResponse).toList();
    }
}
