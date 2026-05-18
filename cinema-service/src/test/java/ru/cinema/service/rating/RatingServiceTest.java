package ru.cinema.service.rating;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cinema.dto.rating.RatingResponse;
import ru.cinema.exception.NotFoundException;
import ru.cinema.model.Content;
import ru.cinema.model.Movie;
import ru.cinema.model.Rating;
import ru.cinema.model.User;
import ru.cinema.model.enums.UserRole;
import ru.cinema.repository.ContentRepository;
import ru.cinema.repository.RatingRepository;
import ru.cinema.service.user.UserService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link RatingService}: rate (happy + recalc), Content NotFound.
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock RatingRepository ratingRepo;
    @Mock ContentRepository contentRepo;
    @Mock UserService userService;

    @InjectMocks RatingService service;

    @Test
    @DisplayName("rate: создаёт новую оценку и пересчитывает средний рейтинг")
    void rate_happyPath_createsAndRecalculatesAverage() {
        User u = new User();
        u.setId(1L);
        u.setRole(UserRole.USER);

        Movie c = new Movie();
        c.setId(5L);
        c.setTitle("Test");

        when(contentRepo.findById(5L)).thenReturn(Optional.of(c));
        when(ratingRepo.findByUserIdAndContentId(1L, 5L)).thenReturn(Optional.empty());
        when(userService.getById(1L)).thenReturn(u);
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> {
            Rating r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(ratingRepo.calculateAverageRating(5L)).thenReturn(8.5);

        RatingResponse resp = service.rate(1L, 5L, 9);

        assertThat(resp.value()).isEqualTo(9);
        assertThat(c.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(8.50));
        verify(contentRepo).save(c);
    }

    @Test
    @DisplayName("rate: бросает NotFoundException, если контента нет")
    void rate_throwsNotFound_whenContentMissing() {
        when(contentRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rate(1L, 999L, 7))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Content");

        verify(ratingRepo, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("delete: бросает NotFoundException, если оценки нет")
    void delete_throwsNotFound_whenRatingMissing() {
        when(ratingRepo.findByUserIdAndContentId(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, 5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Оценка не найдена");
    }
}
