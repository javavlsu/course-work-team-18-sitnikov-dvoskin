package ru.cinema.dto.comment;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.model.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String text,
        Boolean isEdited,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        AuthorRef author
) {
    public static CommentResponse of(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getText(),
                c.getIsEdited(),
                c.getCreatedAt(),
                c.getEditedAt(),
                AuthorRef.of(c.getUser())
        );
    }
}
