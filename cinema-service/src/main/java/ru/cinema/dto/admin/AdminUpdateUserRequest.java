package ru.cinema.dto.admin;

import ru.cinema.model.enums.UserRole;

public record AdminUpdateUserRequest(
        UserRole role,
        Boolean isActive
) {}
