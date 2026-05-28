package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentDetailResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.content.CreateContentRequest;
import ru.cinema.dto.content.UpdateContentRequest;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.service.content.ContentService;

@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "Content", description = "Каталог фильмов и сериалов")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public PageResponse<ContentListItem> list(
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) Long tag,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(contentService.list(type, status, tag, year, country, q, sort, pageable));
    }

    @GetMapping("/{id}")
    public ContentDetailResponse byId(@PathVariable Long id, Authentication auth) {
        Content c = contentService.getById(id);
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (c.getStatus() == ContentStatus.DELETED && !isAdmin) {
            throw NotFoundException.of("Content", id);
        }
        return contentService.toDetail(c);
    }

    /** GET /api/v1/content/top — заявлен в API map Этап 8 (топ по рейтингу). */
    @GetMapping("/top")
    public PageResponse<ContentListItem> top(
            @RequestParam(required = false) ContentType type,
            @PageableDefault(size = 20) Pageable pageable) {
        // sort=rating уже умеет ContentService.list через параметр sort
        return PageResponse.of(contentService.list(type, null, null, null, null, null, "rating", pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ContentDetailResponse create(@Valid @RequestBody CreateContentRequest req) {
        return contentService.toDetail(contentService.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ContentDetailResponse update(@PathVariable Long id, @Valid @RequestBody UpdateContentRequest req) {
        return contentService.toDetail(contentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Публичный «report broken poster» — клиент шлёт когда {@code <img onerror>}
     * сработал на постере. Помечаем запись и она пропадает из листинга.
     * Без auth, идемпотентно, без тела запроса. Невалидный id → 204 (no-op).
     */
    @PostMapping("/{id}/report-broken-poster")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportBrokenPoster(@PathVariable Long id) {
        contentService.markPosterBroken(id);
    }
}
