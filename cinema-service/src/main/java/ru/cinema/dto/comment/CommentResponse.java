package ru.cinema.dto.comment;

import ru.cinema.dto.common.AuthorRef;
import ru.cinema.dto.common.ContentRef;
import ru.cinema.model.Comment;
import ru.cinema.model.enums.CommentStatus;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String text,
        Boolean isEdited,
        CommentStatus status,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        AuthorRef author,
        ContentRef content
) {
    public static CommentResponse of(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getText(),
                c.getIsEdited(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getEditedAt(),
                AuthorRef.of(c.getUser()),
                ContentRef.of(c.getContent())
        );
    }
}
