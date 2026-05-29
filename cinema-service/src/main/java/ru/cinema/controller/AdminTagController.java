package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.tag.CreateTagRequest;
import ru.cinema.dto.tag.TagResponse;
import ru.cinema.service.tag.TagService;

/**
 * AdminTagController — заявлен отдельным контроллером в Этапе 8.
 */
@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Tags")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody CreateTagRequest req) {
        return tagService.create(req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
