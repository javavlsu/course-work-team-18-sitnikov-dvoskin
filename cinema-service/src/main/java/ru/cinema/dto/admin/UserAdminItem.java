package ru.cinema.dto.admin;

import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;

import java.time.LocalDateTime;

public record UserAdminItem(
        Long id,
        String username,
        String email,
        UserRole role,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static UserAdminItem of(User u) {
        return new UserAdminItem(u.getId(), u.getUsername(), u.getEmail(),
                u.getRole(), u.getIsActive(), u.getCreatedAt());
    }
}
