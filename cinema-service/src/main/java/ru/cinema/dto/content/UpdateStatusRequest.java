package ru.cinema.dto.content;

import jakarta.validation.constraints.NotNull;
import ru.cinema.model.enums.ContentStatus;

public record UpdateStatusRequest(
        @NotNull ContentStatus status,
        String reason
) {}
