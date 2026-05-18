package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.admin.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.UpdateStatusRequest;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.model.enums.UserRole;
import ru.cinema.service.admin.AdminService;
import ru.cinema.service.content.ContentService;
import ru.cinema.service.review.ReviewService;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;
    private final ContentService contentService;
    private final ReviewService reviewService;

    public AdminController(AdminService adminService,
                           ContentService contentService,
                           ReviewService reviewService) {
        this.adminService = adminService;
        this.contentService = contentService;
        this.reviewService = reviewService;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return adminService.stats();
    }

    // === Users ===

    @GetMapping("/users")
    public PageResponse<UserAdminItem> users(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listUsers(role, active, pageable).map(UserAdminItem::of));
    }

    @PatchMapping("/users/{id}")
    public UserAdminItem updateUser(@PathVariable Long id,
                                    @Valid @RequestBody AdminUpdateUserRequest req) {
        return UserAdminItem.of(adminService.updateUser(id, req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // === Content ===

    @GetMapping("/content")
    public PageResponse<ContentAdminItem> content(
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) ContentType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listContent(status, type, pageable).map(ContentAdminItem::of));
    }

    @PatchMapping("/content/{id}/status")
    public ContentAdminItem updateContentStatus(@PathVariable Long id,
                                                @Valid @RequestBody UpdateStatusRequest req) {
        return ContentAdminItem.of(contentService.updateStatus(id, req.status()));
    }

    // === Reviews ===

    @GetMapping("/reviews")
    public PageResponse<ReviewAdminItem> reviews(
            @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listReviews(status, pageable).map(ReviewAdminItem::of));
    }

    @PatchMapping("/reviews/{id}/status")
    public ReviewAdminItem updateReviewStatus(@PathVariable Long id,
                                              @Valid @RequestBody UpdateReviewStatusRequest req) {
        return ReviewAdminItem.of(reviewService.updateStatus(id, req.status()));
    }
}
