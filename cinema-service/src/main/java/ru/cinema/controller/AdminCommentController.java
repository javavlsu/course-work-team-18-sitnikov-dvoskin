package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.admin.UpdateCommentStatusRequest;
import ru.cinema.dto.comment.CommentResponse;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.model.enums.CommentStatus;
import ru.cinema.service.comment.CommentService;

/**
 * AdminCommentController — модерация комментариев (use-case Этап 2).
 */
@RestController
@RequestMapping("/api/v1/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public PageResponse<CommentResponse> comments(
            @RequestParam(required = false) CommentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(
                commentService.listByStatus(status == null ? CommentStatus.PUBLISHED : status, pageable));
    }

    @PatchMapping("/{id}/status")
    public CommentResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateCommentStatusRequest req) {
        return commentService.moderate(id, req.status());
    }
}
