package ru.cinema.service.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Маппит публичный sort-параметр на внутренние Spring Data сортировки.
 */
public final class ContentSortMapper {

    private ContentSortMapper() {}

    public static Pageable apply(Pageable input, String sortKey) {
        Sort sort = switch (sortKey == null ? "" : sortKey.toLowerCase()) {
            case "rating", "top" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "new"           -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular"       -> Sort.by(Sort.Direction.DESC, "averageRating");  // прокси: популярность по рейтингу
            case "year"          -> Sort.by(Sort.Direction.DESC, "releaseYear");
            case "alpha"         -> Sort.by(Sort.Direction.ASC, "title");
            default -> input.getSort().isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : input.getSort();
        };
        return PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
    }
}
