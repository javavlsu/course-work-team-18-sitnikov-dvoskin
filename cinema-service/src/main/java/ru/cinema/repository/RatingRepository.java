package ru.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cinema.model.Rating;

import java.util.List;
import java.util.Optional;

/**
 * Компонент доступа к данным для сущности {@link Rating}.
 *
 * <p><b>Назначение:</b> предоставляет методы для работы с оценками контента:
 * создание, обновление, получение и агрегация оценок.</p>
 *
 * <p><b>Связанные use case'ы:</b></p>
 * <ul>
 *     <li>Оценка контента — создание и изменение оценки</li>
 *     <li>Просмотр оценок — получение оценки пользователя и средней оценки</li>
 *     <li>Аналитика системы — статистика оценок</li>
 * </ul>
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Находит оценку, поставленную конкретным пользователем конкретному контенту.
     *
     * <p>Используется для проверки, ставил ли пользователь оценку,
     * и для отображения его текущей оценки на странице контента.</p>
     *
     * @param userId    идентификатор пользователя; должен быть положительным числом
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return {@link Optional} с найденной оценкой, или пустой {@link Optional},
     *         если пользователь не оценивал данный контент
     */
    Optional<Rating> findByUserIdAndContentId(Long userId, Long contentId);

    /**
     * Проверяет, ставил ли пользователь оценку указанному контенту.
     *
     * <p>Используется для быстрой проверки без загрузки всей сущности.</p>
     *
     * @param userId    идентификатор пользователя; должен быть положительным числом
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return {@code true}, если оценка существует; {@code false} — иначе
     */
    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    /**
     * Возвращает все оценки для указанного контента.
     *
     * <p>Используется для отображения распределения оценок на странице контента.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return список всех оценок для данного контента
     */
    List<Rating> findByContentId(Long contentId);

    /**
     * Возвращает все оценки указанного пользователя.
     *
     * <p>Используется в профиле пользователя для отображения
     * истории его оценок.</p>
     *
     * @param userId идентификатор пользователя; должен быть положительным числом
     * @return список всех оценок пользователя
     */
    List<Rating> findByUserId(Long userId);

    /**
     * Вычисляет средний рейтинг для указанного контента.
     *
     * <p>Используется для пересчёта поля {@code averageRating} в таблице {@code content}.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return средний рейтинг в виде {@link Double}, или {@code null},
     *         если контент не имеет оценок
     */
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.content.id = :contentId")
    Double calculateAverageRating(@Param("contentId") Long contentId);

    /**
     * Подсчитывает количество оценок для указанного контента.
     *
     * <p>Используется для отображения количества оценок на карточке контента.</p>
     *
     * @param contentId идентификатор контента; должен быть положительным числом
     * @return количество оценок для данного контента
     */
    long countByContentId(Long contentId);
}
