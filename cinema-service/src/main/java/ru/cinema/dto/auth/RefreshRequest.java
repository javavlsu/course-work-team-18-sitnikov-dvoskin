package ru.cinema.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken обязателен")
        String refreshToken
) {}
