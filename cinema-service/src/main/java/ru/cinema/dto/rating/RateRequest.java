package ru.cinema.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateRequest(
        @NotNull
        @Min(value = 1, message = "оценка от 1 до 10")
        @Max(value = 10, message = "оценка от 1 до 10")
        Integer value
) {}
