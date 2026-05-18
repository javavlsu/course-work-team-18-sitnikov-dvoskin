package ru.cinema.model.enums;

/**
 * Статусы жизненного цикла комментария — заявлены в use-case Этапа 2
 * (модерация комментариев). PUBLISHED — нормальное состояние, HIDDEN —
 * скрыт модератором (виден только автору), DELETED — удалён (soft delete).
 */
public enum CommentStatus {
    /** На модерации — комментарий написан, но ещё не одобрен (use-case Этап 2). */
    MODERATION,
    /** Опубликован — виден всем. */
    PUBLISHED,
    /** Скрыт модератором — виден только автору и админам. */
    HIDDEN,
    /** Удалён (soft delete). */
    DELETED
}
