package ru.cinema.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Size(max = 200) String title,
        String text,
        @Min(1) @Max(10) Integer ratingValue
) {}
