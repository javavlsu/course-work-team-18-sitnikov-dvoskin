package ru.cinema.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.cinema.model.enums.ReviewStatus;

public record UpdateReviewStatusRequest(
        @NotNull ReviewStatus status,
        /** Причина модерации (use-case D Этапа 2, альт-поток 1). */
        @Size(max = 500) String reason
) {}
