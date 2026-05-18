package ru.cinema.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты на доменный метод {@link Content#calculateAverageRating()}.
 */
class ContentTest {

    @Test
    @DisplayName("calculateAverageRating: 0 при отсутствии оценок")
    void calculateAverageRating_returnsZero_whenNoRatings() {
        Content content = new Content();
        content.setRatings(new ArrayList<>());

        content.calculateAverageRating();

        assertThat(content.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateAverageRating: правильное среднее для нескольких оценок")
    void calculateAverageRating_calculatesAverage_whenRatingsPresent() {
        Content content = new Content();
        List<Rating> ratings = List.of(
                ratingOf(8),
                ratingOf(10),
                ratingOf(6)
        );
        content.setRatings(ratings);

        content.calculateAverageRating();

        // (8+10+6)/3 = 8.0
        assertThat(content.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(8.0));
    }

    @Test
    @DisplayName("calculateAverageRating: 0 если список ratings == null")
    void calculateAverageRating_returnsZero_whenRatingsNull() {
        Content content = new Content();
        content.setRatings(null);

        content.calculateAverageRating();

        assertThat(content.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static Rating ratingOf(int value) {
        Rating r = new Rating();
        r.setValue(value);
        return r;
    }
}
