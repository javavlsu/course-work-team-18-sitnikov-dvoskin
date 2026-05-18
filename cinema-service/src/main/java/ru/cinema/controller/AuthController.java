package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.auth.AuthResponse;
import ru.cinema.dto.auth.LoginRequest;
import ru.cinema.dto.auth.RefreshRequest;
import ru.cinema.dto.auth.RegisterRequest;
import ru.cinema.dto.user.UserResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.auth.AuthService;
import ru.cinema.service.user.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Регистрация, вход, refresh, logout")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /** GET /api/v1/auth/me — текущий юзер (по JWT). Алиас на UserController.me, заявлен в API map Этапа 8. */
    @GetMapping("/me")
    public UserResponse me() {
        return userService.toResponse(CurrentUser.currentUser(), true);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
