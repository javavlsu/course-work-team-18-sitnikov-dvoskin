package ru.cinema.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты на доменный метод {@link Rating#changeValue(Integer)}.
 *
 * <p>Покрытие: проверяем что инвариант 1..5 (шкала из ТЗ Этапа 3) соблюдается,
 * и что валидное значение действительно записывается в поле value.</p>
 */
class RatingTest {

    @Test
    @DisplayName("changeValue: задаёт корректное значение в допустимом диапазоне")
    void changeValue_setsValue_whenWithinRange() {
        Rating rating = new Rating();
        rating.setValue(3);

        rating.changeValue(4);

        assertThat(rating.getValue()).isEqualTo(4);
    }

    @Test
    @DisplayName("changeValue: принимает граничные значения 1 и 5")
    void changeValue_acceptsBoundaryValues() {
        Rating rating = new Rating();

        rating.changeValue(1);
        assertThat(rating.getValue()).isEqualTo(1);

        rating.changeValue(5);
        assertThat(rating.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("changeValue: бросает IllegalArgumentException при значении < 1")
    void changeValue_throws_whenBelowMin() {
        Rating rating = new Rating();

        assertThatThrownBy(() -> rating.changeValue(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("от 1 до 5");
    }

    @Test
    @DisplayName("changeValue: бросает IllegalArgumentException при значении > 5")
    void changeValue_throws_whenAboveMax() {
        Rating rating = new Rating();

        assertThatThrownBy(() -> rating.changeValue(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("от 1 до 5");
    }
}
