package ru.cinema.service.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.comment.CommentResponse;
import ru.cinema.dto.comment.CreateCommentRequest;
import ru.cinema.dto.comment.UpdateCommentRequest;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Comment;
import ru.cinema.model.Content;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepo;
    private final ContentRepository contentRepo;
    private final ru.cinema.service.user.UserService userService;

    public CommentService(CommentRepository commentRepo,
                          ContentRepository contentRepo,
                          ru.cinema.service.user.UserService userService) {
        this.commentRepo = commentRepo;
        this.contentRepo = contentRepo;
        this.userService = userService;
    }

    public Page<CommentResponse> listForContent(Long contentId, Pageable pageable) {
        return commentRepo.findByContentIdOrderByCreatedAtAsc(contentId, pageable)
                .map(CommentResponse::of);
    }

    @Transactional
    public CommentResponse create(Long contentId, Long userId, CreateCommentRequest req) {
        Content content = contentRepo.findById(contentId)
                .orElseThrow(() -> NotFoundException.of("Content", contentId));
        Comment c = new Comment();
        c.setUser(userService.getById(userId));
        c.setContent(content);
        c.setText(req.text());
        c.setIsEdited(false);
        return CommentResponse.of(commentRepo.save(c));
    }

    @Transactional
    public CommentResponse update(Long commentId, Long currentUserId, UpdateCommentRequest req) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> NotFoundException.of("Comment", commentId));
        ensureOwner(c, currentUserId);
        c.setText(req.text());
        c.setIsEdited(true);
        c.setEditedAt(LocalDateTime.now());
        return CommentResponse.of(commentRepo.save(c));
    }

    @Transactional
    public void delete(Long commentId, Long currentUserId) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> NotFoundException.of("Comment", commentId));
        User current = userService.getById(currentUserId);
        if (current.getRole() != UserRole.ADMIN
                && !c.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Удалить может только автор или ADMIN");
        }
        commentRepo.delete(c);
    }

    private void ensureOwner(Comment c, Long userId) {
        if (!c.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Только автор может редактировать комментарий");
        }
    }
}
