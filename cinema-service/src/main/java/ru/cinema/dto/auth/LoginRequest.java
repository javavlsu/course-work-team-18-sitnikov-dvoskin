package ru.cinema.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "укажите email или username")
        String emailOrUsername,

        @NotBlank(message = "пароль обязателен")
        String password
) {}
