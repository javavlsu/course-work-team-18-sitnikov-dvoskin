package ru.cinema.service.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.content.CreateContentRequest;
import ru.cinema.dto.content.UpdateContentRequest;
import ru.cinema.model.Content;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;

/**
 * SeriesService — заявлен отдельным сервисом в Этапе 7. Делегирует в
 * {@link ContentService}, фиксируя {@link ContentType#SERIES}.
 */
@Service
public class SeriesService {

    private final ContentService contentService;

    public SeriesService(ContentService contentService) {
        this.contentService = contentService;
    }

    public Page<ContentListItem> list(ContentStatus status, Long tagId, Integer year,
                                       String country, String q, String sort, Pageable pageable) {
        return contentService.list(ContentType.SERIES, status, tagId, year, country, q, sort, pageable);
    }

    public Content getById(Long id) {
        Content c = contentService.getById(id);
        if (c.getContentType() != ContentType.SERIES || c.getStatus() == ru.cinema.model.enums.ContentStatus.DELETED) {
            throw new ru.cinema.exception.NotFoundException("Сериал не найден: " + id);
        }
        return c;
    }

    public Content create(CreateContentRequest req) {
        return contentService.create(req);
    }

    public Content update(Long id, UpdateContentRequest req) {
        getById(id);
        return contentService.update(id, req);
    }

    public void delete(Long id) {
        getById(id);
        contentService.delete(id);
    }
}
