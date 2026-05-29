package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Comment;
import ru.cinema.model.enums.CommentStatus;

import java.util.List;

/**
 * Компонент доступа к данным для сущности {@link Comment}.
 *
 * <p>Связанные use case'ы: комментирование контента, просмотр комментариев,
 * модерация (use-case Этапа 2).</p>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByContentIdOrderByCreatedAtAsc(Long contentId, Pageable pageable);

    /** Публичный список — только комментарии в указанном статусе (обычно PUBLISHED). */
    Page<Comment> findByContentIdAndStatusOrderByCreatedAtAsc(Long contentId, CommentStatus status, Pageable pageable);

    /** Админ-фильтр для модерации. */
    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByContentId(Long contentId);

    long countByContentIdAndStatus(Long contentId, CommentStatus status);

    void deleteByUserId(Long userId);
}
