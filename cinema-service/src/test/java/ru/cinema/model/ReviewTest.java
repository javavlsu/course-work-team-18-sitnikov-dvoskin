package ru.cinema.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.cinema.model.enums.ReviewStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты на доменный метод {@link Review#publish()}.
 */
class ReviewTest {

    @Test
    @DisplayName("publish: переводит рецензию в статус PUBLISHED")
    void publish_setsStatusPublished() {
        Review review = new Review();
        review.setStatus(ReviewStatus.MODERATION);

        review.publish();

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publish: работает даже из статуса DRAFT")
    void publish_worksFromDraft() {
        Review review = new Review();
        review.setStatus(ReviewStatus.DRAFT);

        review.publish();

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    }
}
