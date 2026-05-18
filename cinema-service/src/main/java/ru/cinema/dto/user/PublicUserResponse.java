package ru.cinema.dto.user;

import ru.cinema.model.enums.UserRole;

import java.time.LocalDateTime;

/**
 * DTO с публичной информацией о пользователе (без email).
 */
public record PublicUserResponse(
        Long id,
        String username,
        UserRole role,
        LocalDateTime createdAt,
        UserStats stats
) {}
