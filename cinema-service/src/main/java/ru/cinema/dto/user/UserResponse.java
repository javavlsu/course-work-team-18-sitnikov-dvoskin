package ru.cinema.dto.user;

import ru.cinema.model.enums.UserRole;

import java.time.LocalDateTime;

/**
 * DTO с приватной информацией о пользователе (только для самого пользователя/админа).
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        Boolean isActive,
        LocalDateTime createdAt,
        UserStats stats
) {}
