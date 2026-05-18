package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentDetailResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.content.CreateContentRequest;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.service.content.ContentService;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies")
public class MovieController {

    private final ContentService contentService;

    public MovieController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public PageResponse<ContentListItem> list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(contentService.list(ContentType.MOVIE, ContentStatus.PUBLISHED,
                null, year, country, q, sort, pageable));
    }

    @GetMapping("/{id}")
    public ContentDetailResponse byId(@PathVariable Long id) {
        return contentService.toDetail(contentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ContentDetailResponse create(@Valid @RequestBody CreateContentRequest req) {
        // принудительно MOVIE
        CreateContentRequest forced = new CreateContentRequest(
                ContentType.MOVIE,
                req.title(), req.originalTitle(), req.description(), req.releaseYear(),
                req.posterUrl(), req.country(), req.language(), req.imdbId(), req.kinopoiskId(),
                req.tagIds(),
                req.duration(), req.budget(), req.boxOffice(),
                req.totalSeasons(), req.totalEpisodes(), req.isFinished()
        );
        return contentService.toDetail(contentService.create(forced));
    }
}
