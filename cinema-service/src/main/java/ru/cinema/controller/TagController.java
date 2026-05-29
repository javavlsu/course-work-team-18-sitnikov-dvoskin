package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.tag.CreateTagRequest;
import ru.cinema.dto.tag.TagDetailResponse;
import ru.cinema.dto.tag.TagResponse;
import ru.cinema.service.tag.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponse> all() {
        return tagService.all();
    }

    @GetMapping("/{slug}")
    public TagDetailResponse bySlug(@PathVariable String slug,
                                    @PageableDefault(size = 20) Pageable pageable) {
        return tagService.bySlug(slug, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse create(@Valid @RequestBody CreateTagRequest req) {
        return tagService.create(req);
    }
}
