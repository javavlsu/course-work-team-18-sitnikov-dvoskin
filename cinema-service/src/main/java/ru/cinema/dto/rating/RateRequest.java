package ru.cinema.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateRequest(
        @NotNull
        @Min(value = 1, message = "оценка от 1 до 5")
        @Max(value = 5, message = "оценка от 1 до 5")
        Integer value
) {}
