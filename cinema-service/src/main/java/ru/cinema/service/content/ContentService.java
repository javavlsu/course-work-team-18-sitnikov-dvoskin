package ru.cinema.service.content;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.content.ContentDetailResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.content.CreateContentRequest;
import ru.cinema.dto.content.UpdateContentRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.Series;
import ru.cinema.model.Tag;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.TagRepository;
import ru.cinema.model.enums.ReviewStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository contentRepo;
    private final TagRepository tagRepo;
    private final RatingRepository ratingRepo;
    private final ReviewRepository reviewRepo;
    private final CommentRepository commentRepo;

    public ContentService(ContentRepository contentRepo,
                          TagRepository tagRepo,
                          RatingRepository ratingRepo,
                          ReviewRepository reviewRepo,
                          CommentRepository commentRepo) {
        this.contentRepo = contentRepo;
        this.tagRepo = tagRepo;
        this.ratingRepo = ratingRepo;
        this.reviewRepo = reviewRepo;
        this.commentRepo = commentRepo;
    }

    public Content getById(Long id) {
        return contentRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Content", id));
    }

    public ContentDetailResponse toDetail(Content c) {
        long totalRatings = ratingRepo.countByContentId(c.getId());
        long totalReviews = reviewRepo.countByContentIdAndStatus(c.getId(), ReviewStatus.PUBLISHED);
        long totalComments = commentRepo.countByContentId(c.getId());
        return ContentDetailResponse.of(c, totalRatings, totalReviews, totalComments);
    }

    public Page<ContentListItem> list(ContentType type, ContentStatus status, Long tagId,
                                      Integer year, String country, String q,
                                      String sort, Pageable pageable) {
        Pageable sorted = ContentSortMapper.apply(pageable, sort);

        // be-fts: если задан только текстовый запрос (без иных фильтров) — используем
        // PostgreSQL FTS (быстрее и релевантнее, чем LIKE %q%).
        boolean ftsEligible = q != null && !q.isBlank()
                && type == null && tagId == null && year == null
                && (country == null || country.isBlank())
                && (status == null || status == ContentStatus.PUBLISHED);
        if (ftsEligible) {
            // Сортировка по релевантности уже встроена в native-query через ts_rank.
            // Передаём pageable БЕЗ Sort, иначе Spring Data допишет второй ORDER BY к
            // нативной строке — а ещё и по несуществующей колонке (если sort='rating',
            // Spring Pageable парсит это как Sort.by("rating") до нашего ContentSortMapper).
            // Сущности поднимаем вторым шагом и сохраняем порядок ts_rank.
            org.springframework.data.domain.Pageable ftsPageable = org.springframework.data.domain.PageRequest
                    .of(pageable.getPageNumber(), pageable.getPageSize());
            org.springframework.data.domain.Page<Long> idPage = contentRepo.fullTextSearchIds(q, ftsPageable);
            if (idPage.isEmpty()) {
                return idPage.map(id -> null);
            }
            List<Long> ids = idPage.getContent();
            Map<Long, Content> byId = contentRepo.findAllById(ids).stream()
                    .collect(java.util.stream.Collectors.toMap(Content::getId, c -> c));
            List<ContentListItem> ordered = ids.stream()
                    .map(byId::get)
                    .filter(java.util.Objects::nonNull)
                    .map(ContentListItem::of)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(ordered, pageable, idPage.getTotalElements());
        }

        Specification<Content> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), status == null ? ContentStatus.PUBLISHED : status));
            // Скрываем записи с битой обложкой — флаг ставится клиентом через
            // POST /api/v1/content/{id}/report-broken-poster.
            predicates.add(cb.isFalse(root.get("posterBroken")));
            if (type != null) predicates.add(cb.equal(root.get("contentType"), type));
            if (year != null) predicates.add(cb.equal(root.get("releaseYear"), year));
            if (country != null && !country.isBlank()) predicates.add(cb.equal(root.get("country"), country));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("originalTitle")), like)
                ));
            }
            if (tagId != null) {
                var tags = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(tags.get("id"), tagId));
                cq.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return contentRepo.findAll(spec, sorted).map(ContentListItem::of);
    }

    @Transactional
    public Content create(CreateContentRequest req) {
        if (req.imdbId() != null && contentRepo.findByImdbId(req.imdbId()).isPresent()) {
            throw new ConflictException("Контент с imdbId уже существует: " + req.imdbId());
        }
        if (req.kinopoiskId() != null && contentRepo.findByKinopoiskId(req.kinopoiskId()).isPresent()) {
            throw new ConflictException("Контент с kinopoiskId уже существует: " + req.kinopoiskId());
        }

        Content c;
        if (req.type() == ContentType.MOVIE) {
            Movie m = new Movie();
            m.setDuration(req.duration());
            m.setBudget(req.budget());
            m.setBoxOffice(req.boxOffice());
            c = m;
        } else {
            Series s = new Series();
            s.setTotalSeasons(req.totalSeasons());
            s.setTotalEpisodes(req.totalEpisodes());
            s.setIsFinished(req.isFinished());
            c = s;
        }
        c.setContentType(req.type());
        c.setTitle(req.title());
        c.setOriginalTitle(req.originalTitle());
        c.setDescription(req.description());
        c.setReleaseYear(req.releaseYear());
        c.setPosterUrl(req.posterUrl());
        c.setCountry(req.country());
        c.setLanguage(req.language());
        c.setImdbId(req.imdbId());
        c.setKinopoiskId(req.kinopoiskId());
        c.setStatus(ContentStatus.PUBLISHED);

        applyTags(c, req.tagIds());
        return contentRepo.save(c);
    }

    @Transactional
    public Content update(Long id, UpdateContentRequest req) {
        Content c = getById(id);
        if (req.title() != null) c.setTitle(req.title());
        if (req.originalTitle() != null) c.setOriginalTitle(req.originalTitle());
        if (req.description() != null) c.setDescription(req.description());
        if (req.releaseYear() != null) c.setReleaseYear(req.releaseYear());
        if (req.posterUrl() != null) c.setPosterUrl(req.posterUrl());
        if (req.country() != null) c.setCountry(req.country());
        if (req.language() != null) c.setLanguage(req.language());
        if (req.imdbId() != null) c.setImdbId(req.imdbId());
        if (req.kinopoiskId() != null) c.setKinopoiskId(req.kinopoiskId());
        if (req.status() != null) c.setStatus(req.status());
        if (req.tagIds() != null) applyTags(c, req.tagIds());

        if (c instanceof Movie m) {
            if (req.duration() != null) m.setDuration(req.duration());
            if (req.budget() != null) m.setBudget(req.budget());
            if (req.boxOffice() != null) m.setBoxOffice(req.boxOffice());
        } else if (c instanceof Series s) {
            if (req.totalSeasons() != null) s.setTotalSeasons(req.totalSeasons());
            if (req.totalEpisodes() != null) s.setTotalEpisodes(req.totalEpisodes());
            if (req.isFinished() != null) s.setIsFinished(req.isFinished());
        }
        return contentRepo.save(c);
    }

    @Transactional
    public void delete(Long id) {
        Content c = getById(id);
        // soft delete через статус
        c.setStatus(ContentStatus.DELETED);
        contentRepo.save(c);
    }

    @Transactional
    public Content updateStatus(Long id, ContentStatus status) {
        Content c = getById(id);
        c.setStatus(status);
        return contentRepo.save(c);
    }

    /**
     * Помечает запись как «битый постер» — после этого она исключается из публичных
     * list-выдач (см. фильтр {@code isFalse(posterBroken)} в {@link #list}).
     * Идемпотентно: повторные вызовы для уже помеченной записи ничего не делают.
     * Несуществующий id просто игнорируем — это «реакция на UI-событие», не критичная операция.
     */
    @Transactional
    public void markPosterBroken(Long id) {
        contentRepo.findById(id).ifPresent(c -> {
            if (!c.isPosterBroken()) {
                c.setPosterBroken(true);
                contentRepo.save(c);
            }
        });
    }

    private void applyTags(Content c, List<Long> tagIds) {
        if (tagIds == null) return;
        Set<Tag> oldTags = new HashSet<>(c.getTags());
        c.getTags().clear();
        for (Long tid : tagIds) {
            Tag t = tagRepo.findById(tid).orElseThrow(() -> NotFoundException.of("Tag", tid));
            c.getTags().add(t);
            if (!oldTags.contains(t)) {
                t.setUsageCount((t.getUsageCount() == null ? 0 : t.getUsageCount()) + 1);
            }
        }
        for (Tag old : oldTags) {
            if (!c.getTags().contains(old)) {
                int cur = old.getUsageCount() == null ? 0 : old.getUsageCount();
                old.setUsageCount(Math.max(0, cur - 1));
            }
        }
    }
}
