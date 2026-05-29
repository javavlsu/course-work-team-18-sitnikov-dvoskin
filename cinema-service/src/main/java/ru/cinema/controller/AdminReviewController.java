package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.admin.ReviewAdminItem;
import ru.cinema.dto.admin.UpdateReviewStatusRequest;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.service.admin.AdminService;
import ru.cinema.service.review.ReviewService;

/**
 * AdminReviewController — заявлен отдельным контроллером в Этапе 8.
 */
@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Reviews")
public class AdminReviewController {

    private final AdminService adminService;
    private final ReviewService reviewService;

    public AdminReviewController(AdminService adminService, ReviewService reviewService) {
        this.adminService = adminService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public PageResponse<ReviewAdminItem> reviews(
            @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listReviews(status, pageable).map(ReviewAdminItem::of));
    }

    @PatchMapping("/{id}/status")
    public ReviewAdminItem updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateReviewStatusRequest req) {
        return ReviewAdminItem.of(reviewService.updateStatus(id, req.status(), req.reason()));
    }
}
