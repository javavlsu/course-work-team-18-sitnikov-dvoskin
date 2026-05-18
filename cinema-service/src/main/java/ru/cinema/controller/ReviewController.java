package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.review.CreateReviewRequest;
import ru.cinema.dto.review.LikeResponse;
import ru.cinema.dto.review.ReviewDetailResponse;
import ru.cinema.dto.review.ReviewListItem;
import ru.cinema.dto.review.UpdateReviewRequest;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.review.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public PageResponse<ReviewListItem> list(
            @RequestParam(required = false) Long contentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(reviewService.list(contentId, userId, status, pageable)
                .map(ReviewListItem::of));
    }

    @GetMapping("/{id}")
    public ReviewDetailResponse byId(@PathVariable Long id) {
        Long me = CurrentUser.currentUserId();
        return ReviewDetailResponse.of(reviewService.getById(id), reviewService.hasLiked(id, me));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDetailResponse create(@Valid @RequestBody CreateReviewRequest req) {
        return ReviewDetailResponse.of(reviewService.create(CurrentUser.requireUserId(), req));
    }

    @PutMapping("/{id}")
    public ReviewDetailResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateReviewRequest req) {
        return ReviewDetailResponse.of(reviewService.update(id, CurrentUser.requireUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id, CurrentUser.requireUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ReviewDetailResponse publish(@PathVariable Long id) {
        return ReviewDetailResponse.of(reviewService.publish(id, CurrentUser.requireUserId()));
    }

    @PostMapping("/{id}/like")
    public LikeResponse toggleLike(@PathVariable Long id) {
        return reviewService.toggleLike(id, CurrentUser.requireUserId());
    }

    @PostMapping("/{id}/view")
    public ReviewDetailResponse view(@PathVariable Long id) {
        return ReviewDetailResponse.of(reviewService.incrementView(id));
    }
}
