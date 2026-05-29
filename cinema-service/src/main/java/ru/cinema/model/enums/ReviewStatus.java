package ru.cinema.model.enums;

/**
 * Статусы жизненного цикла рецензии.
 */
public enum ReviewStatus {
    /** Черновик — рецензия создана, но не опубликована. */
    DRAFT,
    /** На модерации — отправлена на проверку. */
    MODERATION,
    /** Опубликована — доступна для просмотра. */
    PUBLISHED,
    /** Отклонена модератором (use-case Этап 2). */
    REJECTED,
    /** Скрыта модератором (use-case Этап 2). */
    HIDDEN,
    /** Удалена (soft delete). */
    DELETED
}
