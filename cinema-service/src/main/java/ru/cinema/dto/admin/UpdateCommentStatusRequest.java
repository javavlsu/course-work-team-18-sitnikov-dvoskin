package ru.cinema.dto.admin;

import jakarta.validation.constraints.NotNull;
import ru.cinema.model.enums.CommentStatus;

public record UpdateCommentStatusRequest(
        @NotNull CommentStatus status
) {}
