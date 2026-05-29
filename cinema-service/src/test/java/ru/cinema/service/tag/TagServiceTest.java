package ru.cinema.service.tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.tag.CreateTagRequest;
import ru.cinema.dto.tag.TagResponse;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Tag;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.TagRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link TagService}: create (happy + duplicate name), bySlug NotFound.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock TagRepository tagRepo;
    @Mock ContentRepository contentRepo;

    @InjectMocks TagService service;

    @Test
    @DisplayName("create: создаёт тег, если имя уникально, и автогенерирует slug")
    void create_happyPath_autoSlug() {
        CreateTagRequest req = new CreateTagRequest("Sci-Fi", null, "научная фантастика");

        when(tagRepo.existsByName("Sci-Fi")).thenReturn(false);
        when(tagRepo.findBySlug("sci-fi")).thenReturn(Optional.empty());
        when(tagRepo.save(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(11L);
            return t;
        });

        TagResponse resp = service.create(req);

        assertThat(resp.id()).isEqualTo(11L);
        assertThat(resp.name()).isEqualTo("Sci-Fi");
        assertThat(resp.slug()).isEqualTo("sci-fi");
        assertThat(resp.usageCount()).isZero();
    }

    @Test
    @DisplayName("create: бросает ConflictException, если имя уже существует")
    void create_throwsConflict_whenNameTaken() {
        CreateTagRequest req = new CreateTagRequest("Sci-Fi", null, null);
        when(tagRepo.existsByName("Sci-Fi")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Тег уже существует");

        verify(tagRepo, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("bySlug: бросает NotFoundException для отсутствующего slug")
    void bySlug_throwsNotFound_whenMissing() {
        when(tagRepo.findBySlug("missing")).thenReturn(Optional.empty());
        // contentRepo не должен вызваться, но ставим lenient на случай рефакторинга
        lenient().when(contentRepo.findByTagId(any(), any(), any())).thenReturn(null);

        assertThatThrownBy(() -> service.bySlug("missing", org.springframework.data.domain.Pageable.unpaged()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Тег не найден");
    }
}
