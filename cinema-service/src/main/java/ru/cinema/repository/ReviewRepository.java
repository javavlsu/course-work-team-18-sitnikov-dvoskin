package ru.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Review;
import ru.cinema.model.enums.ReviewStatus;

import java.util.List;
import java.util.Optional;

/**
 * Компонент доступа к данным для сущности {@link Review}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с рецензиями,
 * включая поиск, фильтрацию по статусу и привязку к пользователю/контенту.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Написание рецензии — создание и сохранение рецензии</li>
 *     <li>Просмотр рецензий — получение рецензий для конкретного контента</li>
 *     <li>Модерация контента / Проверка рецензий — фильтрация рецензий по статусу</li>
 *     <li>Управление профилем — просмотр своих рецензий</li>
 * </ul>
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Возвращает рецензии для указанного контента с заданным статусом.
     *
     * <p>Используется на странице контента для отображения опубликованных рецензий.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @param status    статус рецензии ({@link ReviewStatus}); не должен быть {@code null}
     * @param pageable  параметры пагинации; не должен быть {@code null}
     * @return страница рецензий для указанного контента
     */
    Page<Review> findByContentIdAndStatus(Long contentId, ReviewStatus status, Pageable pageable);

    /**
     * Возвращает все рецензии указанного пользователя.
     *
     * <p>Используется на странице «Мои рецензии» в профиле пользователя.</p>
     *
     * @param userId   идентификатор пользователя; должен быть положительным числом
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница рецензий указанного пользователя
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * Возвращает рецензии с указанным статусом (для модерации).
     *
     * <p>Используется администратором на странице модерации рецензий.</p>
     *
     * @param status   статус рецензии; не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница рецензий с указанным статусом
     */
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    /**
     * Проверяет, существует ли рецензия от пользователя на указанный контент.
     *
     * <p>Используется для предотвращения повторного написания рецензии
     * одним пользователем на один и тот же контент.</p>
     *
     * @param userId    идентификатор пользователя; должен быть положительным числом
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return {@code true}, если рецензия существует; {@code false} — иначе
     */
    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    /**
     * Находит рецензию пользователя на указанный контент.
     *
     * <p>Используется для редактирования существующей рецензии.</p>
     *
     * @param userId    идентификатор пользователя; должен быть положительным числом
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return {@link Optional} с найденной рецензией, или пустой {@link Optional}
     */
    Optional<Review> findByUserIdAndContentId(Long userId, Long contentId);

    /**
     * Возвращает самые популярные рецензии (по количеству лайков).
     *
     * <p>Используется для формирования раздела «Популярные рецензии».</p>
     *
     * @param status   статус рецензии (обычно {@link ReviewStatus#PUBLISHED}); не должен быть {@code null}
     * @param pageable параметры пагинации; не должен быть {@code null}
     * @return страница рецензий, отсортированная по количеству лайков (убывание)
     */
    Page<Review> findByStatusOrderByLikeCountDesc(ReviewStatus status, Pageable pageable);

    /**
     * Подсчитывает количество рецензий для указанного контента.
     *
     * <p>Используется для отображения счётчика рецензий на карточке контента.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @param status    статус рецензии; не должен быть {@code null}
     * @return количество рецензий с указанным статусом для данного контента
     */
    long countByContentIdAndStatus(Long contentId, ReviewStatus status);
}
