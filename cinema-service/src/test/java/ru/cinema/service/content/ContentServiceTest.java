package ru.cinema.service.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.content.CreateContentRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.enums.ContentStatus;
import ru.cinema.model.enums.ContentType;
import ru.cinema.repository.CommentRepository;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.repository.TagRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link ContentService}: getById, create (happy + конфликт по imdbId).
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock ContentRepository contentRepo;
    @Mock TagRepository tagRepo;
    @Mock RatingRepository ratingRepo;
    @Mock ReviewRepository reviewRepo;
    @Mock CommentRepository commentRepo;

    @InjectMocks ContentService service;

    @Test
    @DisplayName("getById: возвращает контент, если найден")
    void getById_returnsContent_whenExists() {
        Movie m = new Movie();
        m.setId(42L);
        m.setTitle("Дюна");
        m.setContentType(ContentType.MOVIE);
        m.setStatus(ContentStatus.PUBLISHED);

        when(contentRepo.findById(42L)).thenReturn(Optional.of(m));

        Content result = service.getById(42L);

        assertThat(result.getTitle()).isEqualTo("Дюна");
        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getById: бросает NotFoundException, если контента нет")
    void getById_throwsNotFound_whenMissing() {
        when(contentRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Content");
    }

    @Test
    @DisplayName("create: успешно создаёт фильм, если imdbId уникален")
    void create_happyPath_movie() {
        CreateContentRequest req = new CreateContentRequest(
                ContentType.MOVIE, "Дюна", "Dune", "Эпик про песок", 2024,
                null, "США", "en", "tt15239678", null, List.of(),
                155, null, null, null, null, null);

        when(contentRepo.findByImdbId("tt15239678")).thenReturn(Optional.empty());
        when(contentRepo.save(any(Content.class))).thenAnswer(inv -> {
            Content c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });

        Content created = service.create(req);

        assertThat(created.getId()).isEqualTo(7L);
        assertThat(created.getTitle()).isEqualTo("Дюна");
        assertThat(created.getContentType()).isEqualTo(ContentType.MOVIE);
        assertThat(created.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(created).isInstanceOf(Movie.class);
        assertThat(((Movie) created).getDuration()).isEqualTo(155);
    }

    @Test
    @DisplayName("create: бросает ConflictException при дубликате imdbId")
    void create_throwsConflict_whenImdbDuplicate() {
        CreateContentRequest req = new CreateContentRequest(
                ContentType.MOVIE, "Дюна", null, null, 2024,
                null, null, null, "tt15239678", null, List.of(),
                null, null, null, null, null, null);

        Movie existing = new Movie();
        existing.setId(1L);
        when(contentRepo.findByImdbId("tt15239678")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("imdbId");

        verify(contentRepo, never()).save(any(Content.class));
    }
}
