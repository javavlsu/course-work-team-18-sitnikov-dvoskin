package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.comment.CommentResponse;
import ru.cinema.dto.comment.CreateCommentRequest;
import ru.cinema.dto.comment.UpdateCommentRequest;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.comment.CommentService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/content/{contentId}/comments")
    public PageResponse<CommentResponse> list(@PathVariable Long contentId,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(commentService.listForContent(contentId, pageable));
    }

    @PostMapping("/content/{contentId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long contentId,
                                  @Valid @RequestBody CreateCommentRequest req) {
        return commentService.create(contentId, CurrentUser.requireUserId(), req);
    }

    @PatchMapping("/comments/{id}")
    public CommentResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateCommentRequest req) {
        return commentService.update(id, CurrentUser.requireUserId(), req);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentService.delete(id, CurrentUser.requireUserId());
        return ResponseEntity.noContent().build();
    }
}
