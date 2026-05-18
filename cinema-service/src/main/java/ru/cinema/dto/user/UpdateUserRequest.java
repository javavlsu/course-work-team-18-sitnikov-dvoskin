package ru.cinema.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление профиля. Все поля опциональны.
 * Если указан newPassword — должен быть указан и currentPassword.
 */
public record UpdateUserRequest(
        @Size(min = 3, max = 50, message = "username должен быть от 3 до 50 символов")
        String username,

        @Email(message = "некорректный формат email")
        @Size(max = 100)
        String email,

        String currentPassword,

        @Size(min = 8, message = "пароль должен быть не короче 8 символов")
        String newPassword
) {}
