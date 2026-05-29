package ru.cinema.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.UnauthorizedException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.UserRepository;

/**
 * Утилита получения текущего пользователя из SecurityContext.
 * Может использоваться как Spring bean или статически.
 */
@Component
public class CurrentUser {

    private static UserRepository userRepoStatic;

    public CurrentUser(UserRepository userRepository) {
        CurrentUser.userRepoStatic = userRepository;
    }

    /** ID текущего пользователя или null, если запрос анонимный. */
    public static Long currentUserId() {
        AuthPrincipal p = principal();
        return p == null ? null : p.id();
    }

    /** ID текущего юзера или 401. */
    public static Long requireUserId() {
        Long id = currentUserId();
        if (id == null) throw new UnauthorizedException("Требуется аутентификация");
        return id;
    }

    /** Возвращает principal или null. */
    public static AuthPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object pr = auth.getPrincipal();
        return (pr instanceof AuthPrincipal ap) ? ap : null;
    }

    public static boolean isAuthenticated() {
        return principal() != null;
    }

    public static boolean hasRole(UserRole role) {
        AuthPrincipal p = principal();
        return p != null && role.name().equals(p.role());
    }

    /** Текущий пользователь из БД. Бросает ForbiddenException, если не залогинен. */
    public static User currentUser() {
        Long id = requireUserId();
        return userRepoStatic.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
    }

    /** Опциональный текущий пользователь (null для анонимов). */
    public static User currentUserOrNull() {
        Long id = currentUserId();
        if (id == null) return null;
        return userRepoStatic.findById(id).orElse(null);
    }
}
