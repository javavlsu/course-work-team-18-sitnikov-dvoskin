package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.admin.ContentAdminItem;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.UpdateStatusRequest;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.service.admin.AdminService;
import ru.cinema.service.content.ContentService;
import ru.cinema.service.tag.TagService;

/**
 * AdminContentController — заявлен отдельным контроллером в Этапе 8.
 * Также содержит привязку тегов к контенту (из API map).
 */
@RestController
@RequestMapping("/api/v1/admin/content")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Content")
public class AdminContentController {

    private final AdminService adminService;
    private final ContentService contentService;
    private final TagService tagService;

    public AdminContentController(AdminService adminService,
                                  ContentService contentService,
                                  TagService tagService) {
        this.adminService = adminService;
        this.contentService = contentService;
        this.tagService = tagService;
    }

    @GetMapping
    public PageResponse<ContentAdminItem> content(
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listContent(status, type, q, pageable).map(ContentAdminItem::of));
    }

    @PatchMapping("/{id}/status")
    public ContentAdminItem updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStatusRequest req) {
        return ContentAdminItem.of(contentService.updateStatus(id, req.status()));
    }

    @PostMapping("/{contentId}/tags/{tagId}")
    public ResponseEntity<Void> attachTag(@PathVariable Long contentId, @PathVariable Long tagId) {
        tagService.attachToContent(contentId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{contentId}/tags/{tagId}")
    public ResponseEntity<Void> detachTag(@PathVariable Long contentId, @PathVariable Long tagId) {
        tagService.detachFromContent(contentId, tagId);
        return ResponseEntity.noContent().build();
    }
}
