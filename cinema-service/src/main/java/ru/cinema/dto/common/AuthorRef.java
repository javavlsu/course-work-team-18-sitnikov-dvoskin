package ru.cinema.dto.common;

import ru.cinema.model.User;

/**
 * Краткая информация об авторе (используется в DTO рецензий, комментариев, подборок).
 */
public record AuthorRef(Long id, String username) {

    public static AuthorRef of(User user) {
        if (user == null) return null;
        return new AuthorRef(user.getId(), user.getUsername());
    }
}
