package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Comment;

import java.util.List;

/**
 * Компонент доступа к данным для сущности {@link Comment}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с комментариями
 * пользователей к контенту.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Комментирование контента — создание и сохранение комментария</li>
 *     <li>Просмотр комментариев — получение комментариев к контенту</li>
 *     <li>Модерация контента / Проверка комментариев — фильтрация и удаление</li>
 * </ul>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Возвращает комментарии к указанному контенту, отсортированные по дате создания.
     *
     * <p>Используется на странице контента для отображения всех комментариев
     * в хронологическом порядке (новые внизу).</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @param pageable  параметры пагинации; не должен быть {@code null}
     * @return страница комментариев для указанного контента
     */
    Page<Comment> findByContentIdOrderByCreatedAtAsc(Long contentId, Pageable pageable);

    /**
     * Возвращает все комментарии указанного пользователя.
     *
     * <p>Используется в профиле пользователя и при модерации
     * для просмотра всех комментариев конкретного пользователя.</p>
     *
     * @param userId идентификатор пользователя; должен быть положительным числом
     * @return список комментариев пользователя, отсортированный по дате создания
     */
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Подсчитывает количество комментариев к указанному контенту.
     *
     * <p>Используется для отображения счётчика комментариев на карточке контента.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return количество комментариев к данному контенту
     */
    long countByContentId(Long contentId);

    /**
     * Удаляет все комментарии указанного пользователя.
     *
     * <p>Используется при блокировке пользователя для удаления
     * всех его комментариев из системы.</p>
     *
     * @param userId идентификатор пользователя; должен быть положительным числом
     */
    void deleteByUserId(Long userId);
}
