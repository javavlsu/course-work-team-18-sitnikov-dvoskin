package ru.cinema.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "текст обязателен")
        @Size(max = 2000, message = "до 2000 символов")
        String text
) {}
