package ru.cinema.exception;

/**
 * 401 Unauthorized — токена нет, или он невалидный/протух.
 * Отличается от {@link ForbiddenException} (403): тот для «залогинен, но нет прав».
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
