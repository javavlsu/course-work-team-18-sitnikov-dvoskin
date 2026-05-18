package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.service.content.ContentService;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search")
public class SearchController {

    private final ContentService contentService;

    public SearchController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public PageResponse<ContentListItem> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) Long tag,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @PageableDefault(size = 20) Pageable pageable) {
        // yearFrom/yearTo пока схлопнем в один год при наличии (для простоты курсача)
        Integer year = (yearFrom != null && yearFrom.equals(yearTo)) ? yearFrom : null;
        return PageResponse.of(contentService.list(type, ContentStatus.PUBLISHED,
                tag, year, null, q, "new", pageable));
    }
}
