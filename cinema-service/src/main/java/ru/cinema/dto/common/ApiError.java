package ru.cinema.dto.common;

import java.time.Instant;
import java.util.List;

/**
 * Стандартный формат ошибки API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError validation(String message, String path, List<FieldError> errors) {
        return new ApiError(Instant.now(), 400, "Bad Request", message, path, errors);
    }
}
