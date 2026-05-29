package ru.cinema.service.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.dto.content.ContentListItem;
import ru.cinema.dto.tag.CreateTagRequest;
import ru.cinema.dto.tag.TagDetailResponse;
import ru.cinema.dto.tag.TagResponse;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Tag;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.TagRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepo;
    private final ContentRepository contentRepo;

    public TagService(TagRepository tagRepo, ContentRepository contentRepo) {
        this.tagRepo = tagRepo;
        this.contentRepo = contentRepo;
    }

    public List<TagResponse> all() {
        return tagRepo.findAllByOrderByUsageCountDesc().stream().map(TagResponse::of).toList();
    }

    public TagDetailResponse bySlug(String slug, Pageable pageable) {
        Tag tag = tagRepo.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Тег не найден: " + slug));
        var page = contentRepo.findByTagId(tag.getId(), ContentStatus.PUBLISHED, pageable)
                .map(ContentListItem::of);
        return TagDetailResponse.of(tag, PageResponse.of(page));
    }

    @Transactional
    public TagResponse create(CreateTagRequest req) {
        if (tagRepo.existsByName(req.name())) {
            throw new ConflictException("Тег уже существует: " + req.name());
        }
        Tag t = new Tag();
        t.setName(req.name());
        t.setDescription(req.description());
        String slug = (req.slug() == null || req.slug().isBlank()) ? slugify(req.name()) : req.slug();
        if (tagRepo.findBySlug(slug).isPresent()) {
            throw new ConflictException("Slug уже занят: " + slug);
        }
        t.setSlug(slug);
        t.setUsageCount(0);
        return TagResponse.of(tagRepo.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!tagRepo.existsById(id)) throw new NotFoundException("Тег не найден: " + id);
        tagRepo.deleteById(id);
    }

    @Transactional
    public void attachToContent(Long contentId, Long tagId) {
        var content = contentRepo.findById(contentId)
                .orElseThrow(() -> new NotFoundException("Контент не найден: " + contentId));
        var tag = tagRepo.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Тег не найден: " + tagId));
        if (content.getTags() == null) content.setTags(new java.util.HashSet<>());
        if (content.getTags().add(tag)) {
            tag.setUsageCount((tag.getUsageCount() == null ? 0 : tag.getUsageCount()) + 1);
            tagRepo.save(tag);
        }
        contentRepo.save(content);
    }

    @Transactional
    public void detachFromContent(Long contentId, Long tagId) {
        var content = contentRepo.findById(contentId)
                .orElseThrow(() -> new NotFoundException("Контент не найден: " + contentId));
        var tag = tagRepo.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Тег не найден: " + tagId));
        if (content.getTags() != null && content.getTags().remove(tag)) {
            int n = (tag.getUsageCount() == null ? 0 : tag.getUsageCount()) - 1;
            tag.setUsageCount(Math.max(0, n));
            tagRepo.save(tag);
        }
        contentRepo.save(content);
    }

    static String slugify(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^a-z0-9а-яё]+", "-").replaceAll("(^-+)|(-+$)", "");
    }
}
