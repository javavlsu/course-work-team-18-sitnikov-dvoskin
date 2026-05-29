package ru.cinema.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username обязателен")
        @Size(min = 3, max = 50, message = "username от 3 до 50 символов")
        String username,

        @NotBlank(message = "email обязателен")
        @Email(message = "некорректный формат email")
        @Size(max = 100)
        String email,

        @NotBlank(message = "пароль обязателен")
        @Size(min = 8, message = "пароль не короче 8 символов")
        String password
) {}
