package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.model.enums.ContentType;
import ru.cinema.service.content.ContentService;

/**
 * Универсальный поиск по фильмам и сериалам (use-case A из Этапа 2).
 * Поддерживает все фильтры заявленные в ТЗ:
 * <ul>
 *   <li><b>q</b> — текст по названию и оригинальному названию (incl. неполные совпадения);</li>
 *   <li><b>type</b> — MOVIE / SERIES;</li>
 *   <li><b>tag</b> / <b>genre</b> — фильтр по тегу или жанру (М:N);</li>
 *   <li><b>yearFrom</b> / <b>yearTo</b> — диапазон лет выпуска;</li>
 *   <li><b>minRating</b> — минимальная средняя оценка (1–5);</li>
 *   <li><b>person</b> + <b>personRole</b>={ACTOR|DIRECTOR} — поиск по актёру/режиссёру (use-case Этапа 2);</li>
 *   <li><b>country</b> — точное совпадение страны;</li>
 *   <li><b>sort</b> — new/rating/title/year.</li>
 * </ul>
 */
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
            @RequestParam(required = false) Long genre,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String person,
            @RequestParam(required = false) String personRole,
            @RequestParam(required = false) String country,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(contentService.search(
                type, tag, genre, person, personRole,
                yearFrom, yearTo, minRating,
                country, q, sort, pageable));
    }
}
