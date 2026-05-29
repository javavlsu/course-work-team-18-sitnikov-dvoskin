package ru.cinema.service.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.review.CreateReviewRequest;
import ru.cinema.exception.ConflictException;
import ru.cinema.exception.ForbiddenException;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.Review;
import ru.cinema.model.User;
import ru.cinema.model.enums.ReviewStatus;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.ReviewRepository;
import ru.cinema.service.user.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link ReviewService}: create happy/conflict, delete forbidden.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepo;
    @Mock ContentRepository contentRepo;
    @Mock UserService userService;

    @InjectMocks ReviewService service;

    private User user(Long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setUsername("u" + id);
        u.setRole(role);
        u.setIsActive(true);
        return u;
    }

    private Content content(Long id) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle("Test movie");
        return m;
    }

    @Test
    @DisplayName("create: создаёт черновик рецензии, если пользователь ещё не писал на этот контент")
    void create_happyPath_savesAsDraft() {
        Content c = content(5L);
        User author = user(1L, UserRole.USER);
        CreateReviewRequest req = new CreateReviewRequest(5L, "Заголовок", "Большой текст рецензии", 9);

        when(contentRepo.findById(5L)).thenReturn(Optional.of(c));
        when(reviewRepo.existsByUserIdAndContentId(1L, 5L)).thenReturn(false);
        when(userService.getById(1L)).thenReturn(author);
        when(reviewRepo.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(77L);
            return r;
        });

        Review created = service.create(1L, req);

        assertThat(created.getId()).isEqualTo(77L);
        assertThat(created.getTitle()).isEqualTo("Заголовок");
        assertThat(created.getRatingValue()).isEqualTo(9);
        assertThat(created.getStatus()).isEqualTo(ReviewStatus.DRAFT);
        assertThat(created.getUser()).isEqualTo(author);
        assertThat(created.getContent()).isEqualTo(c);
    }

    @Test
    @DisplayName("create: бросает ConflictException, если пользователь уже оставлял рецензию")
    void create_throwsConflict_whenAlreadyReviewed() {
        Content c = content(5L);
        CreateReviewRequest req = new CreateReviewRequest(5L, "T", "Text", 7);

        when(contentRepo.findById(5L)).thenReturn(Optional.of(c));
        when(reviewRepo.existsByUserIdAndContentId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже написали рецензию");

        verify(reviewRepo, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("delete: USER не может удалить чужую рецензию (ForbiddenException)")
    void delete_throwsForbidden_whenNotOwnerAndNotAdmin() {
        Review r = new Review();
        r.setId(10L);
        r.setUser(user(99L, UserRole.USER));   // автор — не текущий пользователь
        r.setContent(content(5L));
        r.setStatus(ReviewStatus.PUBLISHED);

        when(reviewRepo.findById(10L)).thenReturn(Optional.of(r));
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.USER));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(ForbiddenException.class);

        verify(reviewRepo, never()).delete(any(Review.class));
    }
}
