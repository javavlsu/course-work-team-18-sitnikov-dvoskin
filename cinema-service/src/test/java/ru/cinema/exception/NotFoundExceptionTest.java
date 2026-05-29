package ru.cinema.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты на фабрику {@link NotFoundException#of(String, Object)}.
 */
class NotFoundExceptionTest {

    @Test
    @DisplayName("of: формирует сообщение вида '<entity> не найден: id=<id>'")
    void of_formatsMessageCorrectly() {
        NotFoundException ex = NotFoundException.of("Content", 42L);

        assertThat(ex)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Content не найден: id=42");
    }

    @Test
    @DisplayName("of: бросается через assertThatThrownBy и матчится по типу")
    void of_isThrowable() {
        assertThatThrownBy(() -> {
            throw NotFoundException.of("Review", 999L);
        })
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Review");
    }
}
