package ru.cinema.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "contentId обязателен")
        Long contentId,

        @NotBlank(message = "заголовок обязателен")
        @Size(max = 200, message = "заголовок до 200 символов")
        String title,

        @NotBlank(message = "текст рецензии обязателен")
        String text,

        @Min(value = 1, message = "оценка от 1 до 10")
        @Max(value = 10, message = "оценка от 1 до 10")
        Integer ratingValue
) {}
