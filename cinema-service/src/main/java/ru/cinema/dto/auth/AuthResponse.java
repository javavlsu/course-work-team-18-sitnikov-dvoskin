package ru.cinema.dto.auth;

import ru.cinema.dto.user.UserResponse;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
