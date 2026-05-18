package ru.cinema.dto.admin;

import jakarta.validation.constraints.NotNull;
import ru.cinema.model.enums.ReviewStatus;

public record UpdateReviewStatusRequest(
        @NotNull ReviewStatus status
) {}
